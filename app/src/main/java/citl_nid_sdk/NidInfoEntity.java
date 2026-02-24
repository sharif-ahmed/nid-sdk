package citl_nid_sdk;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "nid_info")
public class NidInfoEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nidNumber;
    private String fullName;
    private String dateOfBirth;
    private String frontImagePath;
    private String backImagePath;
    private long createdAt;

    public NidInfoEntity() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public int getId() { return id; }
    public String getNidNumber() { return nidNumber; }
    public String getFullName() { return fullName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getFrontImagePath() { return frontImagePath; }
    public String getBackImagePath() { return backImagePath; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNidNumber(String nidNumber) { this.nidNumber = nidNumber; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setFrontImagePath(String frontImagePath) { this.frontImagePath = frontImagePath; }
    public void setBackImagePath(String backImagePath) { this.backImagePath = backImagePath; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
