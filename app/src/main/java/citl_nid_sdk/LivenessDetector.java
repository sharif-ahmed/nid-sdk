package citl_nid_sdk;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Professional Banking-Level Step-by-Step Liveness Detection System
 * 
 * Features:
 * - Sequential challenge-response system (one action at a time)
 * - Random action order generation
 * - Smooth progress interpolation
 * - Face-in-oval guide detection
 * - Production-ready error handling
 */
public class LivenessDetector {

    /**
     * Supported liveness action types
     */
    public enum ActionType {
        BLINK,
        SMILE,
        TURN_HEAD_LEFT,
        TURN_HEAD_RIGHT
    }

    /**
     * Callback interface for liveness detection results
     */
    public interface Callback {
        void onResult(LivenessResult result);
    }

    /**
     * Callback interface for action progress updates
     */
    public interface ActionProgressCallback {
        /**
         * Called when current action progress updates
         * @param currentAction The action currently being performed
         * @param progress Progress value 0.0f to 1.0f
         * @param completed True if action just completed
         */
        void onActionProgress(ActionType currentAction, float progress, boolean completed);
        
        /**
         * Called when an action is completed
         * @param action The completed action
         */
        void onActionCompleted(ActionType action);
        
        /**
         * Called when all actions are completed
         */
        void onAllActionsCompleted();
    }

    /**
     * Callback interface for face guide oval detection
     */
    public interface FaceGuideCallback {
        /**
         * Called when face position relative to oval guide changes
         * @param faceInOval True if face bounding box is inside oval guide
         * @param guideProgress Progress of face alignment (0.0f to 1.0f)
         */
        void onFaceGuideUpdate(boolean faceInOval, float guideProgress);
    }

    /**
     * Internal state tracking for liveness detection
     */
    public static class LivenessState {
        // Completion flags
        public boolean blinkDone = false;
        public boolean smileDone = false;
        public boolean headTurnLeftDone = false;
        public boolean headTurnRightDone = false;

        // Progress values (0.0f to 1.0f)
        public float blinkProgress = 0f;
        public int blinkCount = 0;
        public float smileProgress = 0f;
        public float headTurnLeftProgress = 0f;
        public float headTurnRightProgress = 0f;

        /**
         * Check if all required actions are completed
         */
        public boolean isAllCompleted() {
            return blinkDone && smileDone && (headTurnLeftDone || headTurnRightDone);
        }
    }

    // Constants
    private static final float PROGRESS_INTERPOLATION_FACTOR = 0.1f;
    private static final float BLINK_THRESHOLD = 0.2f;
    private static final float SMILE_THRESHOLD = 0.7f;
    private static final float HEAD_TURN_THRESHOLD = 20.0f;
    private static final float HEAD_TURN_MAX_ANGLE = 25.0f;

    // ML Kit Face Detector
    private final FaceDetector detector;

    // State management
    private LivenessState state = new LivenessState();
    private List<ActionType> actionSequence = new ArrayList<>();
    private int currentActionIndex = 0;
    
    // Callbacks
    private ActionProgressCallback progressCallback;
    private FaceGuideCallback guideCallback;
    
    // Face guide oval bounds (set by UI)
    private RectF ovalGuideBounds = null;
    private boolean faceInOval = false;
    private float guideProgress = 0f;
    private boolean isLastBlinkStateClosed = false;

    /**
     * Constructor - Initializes ML Kit Face Detector and generates random action sequence
     */
    public LivenessDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build();
        detector = com.google.mlkit.vision.face.FaceDetection.getClient(options);
        generateRandomActionSequence();
    }

    /**
     * Set callback for action progress updates
     */
    public void setProgressCallback(@Nullable ActionProgressCallback callback) {
        this.progressCallback = callback;
    }

    /**
     * Set callback for face guide oval updates
     */
    public void setFaceGuideCallback(@Nullable FaceGuideCallback callback) {
        this.guideCallback = callback;
    }

    /**
     * Set the oval guide bounds for face alignment detection
     * @param bounds RectF representing the oval guide area (in screen coordinates)
     */
    public void setOvalGuideBounds(@Nullable RectF bounds) {
        this.ovalGuideBounds = bounds;
    }

    /**
     * Generate random action sequence
     * Always includes: BLINK, SMILE, and one HEAD_TURN (left or right)
     */
    public void generateRandomActionSequence() {
        actionSequence.clear();
        actionSequence.add(ActionType.BLINK);
        actionSequence.add(ActionType.SMILE);
        
        // Randomly choose left or right head turn
        if (Math.random() > 0.5) {
            actionSequence.add(ActionType.TURN_HEAD_LEFT);
        } else {
            actionSequence.add(ActionType.TURN_HEAD_RIGHT);
        }
        
        // Shuffle for random order
        Collections.shuffle(actionSequence);
        
        // Reset state
        currentActionIndex = 0;
        state = new LivenessState();
    }

    /**
     * Get the current active action
     * @return Current ActionType or null if all actions completed
     */
    @Nullable
    public ActionType getCurrentAction() {
        if (currentActionIndex < actionSequence.size()) {
            return actionSequence.get(currentActionIndex);
        }
        return null;
    }

    /**
     * Get the complete action sequence
     * @return List of ActionType in execution order
     */
    @NonNull
    public List<ActionType> getActionSequence() {
        return new ArrayList<>(actionSequence);
    }

    /**
     * Get current instruction text for UI display
     * @return Instruction string for current action
     */
    @NonNull
    public String getCurrentInstruction() {
        ActionType current = getCurrentAction();
        if (current == null) {
            return "Liveness verification completed!";
        }
        
        switch (current) {
            case BLINK:
                return "Please Blink Eye";
            case SMILE:
                return "Please Smile";
            case TURN_HEAD_LEFT:
                return "Turn Head Left";
            case TURN_HEAD_RIGHT:
                return "Turn Head Right";
            default:
                return "Position your face in the oval";
        }
    }

    /**
     * Check if all liveness actions are completed
     * @return True if all actions completed
     */
    public boolean isLivenessCompleted() {
        return state.isAllCompleted();
    }

    /**
     * Get current liveness state
     * @return LivenessState object
     */
    @NonNull
    public LivenessState getState() {
        return state;
    }

    /**
     * Check if face is currently in oval guide
     * @return True if face bounding box is inside oval guide
     */
    public boolean isFaceInOval() {
        return faceInOval;
    }

    /**
     * Get face guide alignment progress
     * @return Progress value 0.0f to 1.0f
     */
    public float getGuideProgress() {
        return guideProgress;
    }

    /**
     * Get progress of the current step only (for oval round progress UI).
     * Returns 0 when no current action or all completed.
     * @return Progress value 0.0f to 1.0f for the active step
     */
    public float getCurrentStepProgress() {
        ActionType current = getCurrentAction();
        if (current == null) return 0f;
        switch (current) {
            case BLINK: return state.blinkProgress;
            case SMILE: return state.smileProgress;
            case TURN_HEAD_LEFT: return state.headTurnLeftProgress;
            case TURN_HEAD_RIGHT: return state.headTurnRightProgress;
            default: return 0f;
        }
    }

    /**
     * Analyze frame using ML Kit Face Detection
     * This is the main entry point for frame analysis
     */
    public void analyzeFrame(@NonNull InputImage image, @NonNull Callback callback) {
        detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        LivenessResult result = new LivenessResult();
                        
                        if (faces.isEmpty()) {
                            result.isLive = false;
                            result.failureReason = "No face detected";
                            result.faces = faces;
                            callback.onResult(result);
                            return;
                        }

                        // Process face for liveness detection
                        processFace(faces);

                        // Update result
                        result.isLive = isLivenessCompleted();
                        result.faces = faces;
                        if (!result.isLive) {
                            result.failureReason = "Liveness verification in progress";
                        }
                        
                        callback.onResult(result);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LivenessResult result = new LivenessResult();
                        result.isLive = false;
                        result.failureReason = e.getMessage();
                        callback.onResult(result);
                    }
                });
    }

    /**
     * Process detected faces for liveness actions
     * Only processes the current active action
     * 
     * @param faces List of detected faces
     * @return Updated LivenessState
     */
    @NonNull
    public LivenessState processFace(@NonNull List<Face> faces) {
        if (faces.isEmpty()) {
            return state;
        }

        // Check if all actions completed
        if (currentActionIndex >= actionSequence.size()) {
            return state;
        }

        Face face = faces.get(0);
        ActionType currentAction = getCurrentAction();

        if (currentAction == null) {
            return state;
        }

        // Detect face position relative to oval guide
        detectFaceInOval(face);

        // Process only the current action
        float targetProgress = 0f;
        boolean actionCompleted = false;

        switch (currentAction) {
            case BLINK:
                detectBlink(face);
                // Progress is based on blink count (0 to 4)
                state.blinkProgress = interpolateProgress(state.blinkProgress, state.blinkCount / 4.0f);
                if (state.blinkCount >= 4 && !state.blinkDone) {
                    state.blinkDone = true;
                    actionCompleted = true;
                }
                break;

            case SMILE:
                targetProgress = detectSmile(face);
                // Smooth interpolation - only update current action progress
                state.smileProgress = interpolateProgress(state.smileProgress, targetProgress);
                if (state.smileProgress >= 0.9f && !state.smileDone) {
                    state.smileDone = true;
                    actionCompleted = true;
                }
                break;

            case TURN_HEAD_LEFT:
                targetProgress = detectHeadTurnLeft(face);
                // Smooth interpolation - only update current action progress
                state.headTurnLeftProgress = interpolateProgress(state.headTurnLeftProgress, targetProgress);
                if (state.headTurnLeftProgress >= 0.9f && !state.headTurnLeftDone) {
                    state.headTurnLeftDone = true;
                    actionCompleted = true;
                }
                break;

            case TURN_HEAD_RIGHT:
                targetProgress = detectHeadTurnRight(face);
                // Smooth interpolation - only update current action progress
                state.headTurnRightProgress = interpolateProgress(state.headTurnRightProgress, targetProgress);
                if (state.headTurnRightProgress >= 0.9f && !state.headTurnRightDone) {
                    state.headTurnRightDone = true;
                    actionCompleted = true;
                }
                break;
        }

        // Notify progress callback
        if (progressCallback != null) {
            float currentProgress = getCurrentActionProgress(currentAction);
            progressCallback.onActionProgress(currentAction, currentProgress, actionCompleted);
        }

        // Move to next action if current is completed
        if (actionCompleted) {
            // Notify completion of current action
            if (progressCallback != null) {
                progressCallback.onActionCompleted(currentAction);
            }

            // Move to next action
            currentActionIndex++;

            // Check if all actions completed
            if (currentActionIndex >= actionSequence.size()) {
                if (progressCallback != null) {
                    progressCallback.onAllActionsCompleted();
                }
            }
        }

        return state;
    }

    /**
     * Detect blink action progress and count blinks
     * @param face Detected face
     * @return Current blink count progress (internally updates state.blinkCount)
     */
    private float detectBlink(@NonNull Face face) {
        Float leftEyeProb = face.getLeftEyeOpenProbability();
        Float rightEyeProb = face.getRightEyeOpenProbability();

        if (leftEyeProb == null || rightEyeProb == null) {
            return state.blinkCount / 4.0f;
        }

        float avgProb = (leftEyeProb + rightEyeProb) / 2.0f;

        // Transition logic for counting blinks
        if (!isLastBlinkStateClosed && avgProb < BLINK_THRESHOLD) {
            // Eyes just closed
            isLastBlinkStateClosed = true;
        } else if (isLastBlinkStateClosed && avgProb > 0.5f) {
            // Eyes just opened after being closed
            isLastBlinkStateClosed = false;
            if (state.blinkCount < 4) {
                state.blinkCount++;
            }
        }

        return state.blinkCount / 4.0f;
    }

    /**
     * Detect smile action progress
     * @param face Detected face
     * @return Progress value 0.0f to 1.0f
     */
    private float detectSmile(@NonNull Face face) {
        Float smileProb = face.getSmilingProbability();

        if (smileProb == null) {
            return state.smileProgress; // Keep current progress
        }

        // Progress increases with smile probability
        float progress = smileProb; // 0..1
        return clamp01(progress);
    }

    /**
     * Detect head turn left action progress
     * @param face Detected face
     * @return Progress value 0.0f to 1.0f
     */
    private float detectHeadTurnLeft(@NonNull Face face) {
        float angleY = face.getHeadEulerAngleY();

        // Positive angle = left turn
        if (angleY <= 0) {
            return state.headTurnLeftProgress; // Keep current progress
        }

        // Progress increases with left turn angle
        float progress = angleY / HEAD_TURN_MAX_ANGLE;
        return clamp01(progress);
    }

    /**
     * Detect head turn right action progress
     * @param face Detected face
     * @return Progress value 0.0f to 1.0f
     */
    private float detectHeadTurnRight(@NonNull Face face) {
        float angleY = face.getHeadEulerAngleY();

        // Negative angle = right turn
        if (angleY >= 0) {
            return state.headTurnRightProgress; // Keep current progress
        }

        float absAngle = Math.abs(angleY);

        // Progress increases with right turn angle
        float progress = absAngle / HEAD_TURN_MAX_ANGLE;
        return clamp01(progress);
    }

    /**
     * Detect if face bounding box is inside oval guide
     * @param face Detected face
     */
    private void detectFaceInOval(@NonNull Face face) {
        if (ovalGuideBounds == null) {
            return;
        }
        
        android.graphics.Rect boundingBox = face.getBoundingBox();
        RectF faceRect = new RectF(boundingBox);
        
        // Check if face center is inside oval bounds
        float faceCenterX = faceRect.centerX();
        float faceCenterY = faceRect.centerY();
        
        // Simple rectangular check (can be enhanced with actual oval math)
        boolean inBounds = ovalGuideBounds.contains(faceCenterX, faceCenterY);
        
        // Calculate alignment progress based on how centered the face is
        float centerX = ovalGuideBounds.centerX();
        float centerY = ovalGuideBounds.centerY();
        
        float distanceX = Math.abs(faceCenterX - centerX);
        float distanceY = Math.abs(faceCenterY - centerY);
        
        float maxDistanceX = ovalGuideBounds.width() / 2.0f;
        float maxDistanceY = ovalGuideBounds.height() / 2.0f;
        
        float progressX = Math.max(0.0f, 1.0f - (distanceX / maxDistanceX));
        float progressY = Math.max(0.0f, 1.0f - (distanceY / maxDistanceY));
        
        float newGuideProgress = (progressX + progressY) / 2.0f;
        
        // Smooth interpolation
        guideProgress = interpolateProgress(guideProgress, newGuideProgress);
        
        // Update state
        boolean wasInOval = faceInOval;
        faceInOval = inBounds && guideProgress > 0.7f;
        
        // Notify callback if state changed
        if (guideCallback != null && (wasInOval != faceInOval || Math.abs(guideProgress - newGuideProgress) > 0.05f)) {
            guideCallback.onFaceGuideUpdate(faceInOval, guideProgress);
        }
    }

    /**
     * Smooth progress interpolation
     * @param current Current progress value
     * @param target Target progress value
     * @return Interpolated progress value
     */
    private float interpolateProgress(float current, float target) {
        return current + (target - current) * PROGRESS_INTERPOLATION_FACTOR;
    }

    /**
     * Get progress value for current action
     * @param action Action type
     * @return Progress value 0.0f to 1.0f
     */
    private float getCurrentActionProgress(@NonNull ActionType action) {
        switch (action) {
            case BLINK:
                return state.blinkProgress;
            case SMILE:
                return state.smileProgress;
            case TURN_HEAD_LEFT:
                return state.headTurnLeftProgress;
            case TURN_HEAD_RIGHT:
                return state.headTurnRightProgress;
            default:
                return 0f;
        }
    }

    /**
     * Clamp value to [0,1]
     */
    private float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    /**
     * Cleanup resources
     */
    public void release() {
        // ML Kit detector is managed by singleton, no explicit cleanup needed
        progressCallback = null;
        guideCallback = null;
        ovalGuideBounds = null;
    }
}
