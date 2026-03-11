# How to Provide the SDK to Clients

To provide the **CITL NID SDK** to a client professionally, follow these steps to prepare a distribution ZIP package.

## 📦 Recommended Package Structure

Create a folder named `CITL_NID_SDK_v1.0.0` with the following structure:

```text
CITL_NID_SDK_v1.0.0/
├── bin/
│   └── citl-nid-sdk-v1.0.0-YYYYMMDD-release.aar      # The actual SDK library
├── docs/
│   └── integration_guide.md          # Professional integration documentation
├── sample/                           # (Optional) Small project demonstrating usage
│   └── ...                           
└── README.txt                        # Brief overview and version history
```

## 🛠️ Step-by-Step Preparation

1.  **Generate the Library:**
    Run the following command in the project root:
    ```cmd
    .\gradlew :nid_sdk:assembleRelease
    ```
    The output will be at: `nid_sdk\build\outputs\aar\citl-nid-sdk-v1.0.0-YYYYMMDD-release.aar`.

2.  **Prepare the Documentation:**
    Include the `docs/integration_guide.md` file I created. It contains all the technical details the client's developer needs.

3.  **Include License Info:**
    If you provide an API Key, include it in a secure way (e.g., via email or a separate license file).

4.  **Zip and Deliver:**
    Compress the folder into `CITL_NID_SDK_v1.0.0.zip` and send it to the client.

## 📄 4. Generating the PDF Version

To provide a professional PDF version of the `PRODUCTION_DOCUMENTATION.md` to your client, follow these recommended methods:

### Method A: Using VS Code (Easiest & Best Looking)
1.  Install the **"Markdown PDF"** extension (by yyzhang) in VS Code.
2.  Open `docs/PRODUCTION_DOCUMENTATION.md`.
3.  Right-click anywhere in the editor and select **"Markdown PDF: Export (pdf)"**.
4.  A professional PDF will be generated in the same folder.

### Method B: Online Conversion
1.  Go to [Dillinger.io](https://dillinger.io/) or [StackEdit](https://stackedit.io/).
2.  Paste the content of `PRODUCTION_DOCUMENTATION.md`.
3.  Select **Export > As PDF**.

### Method C: Browser Print
1.  Open the `.md` file in a browser that supports Markdown rendering (or use a GitHub preview).
2.  Press `Ctrl + P` (Print).
3.  Select **"Save as PDF"** as the destination.

---

## 💡 Professional Tip
Always provide **both** the `.md` file (for their developers) and the `.pdf` file (for their project managers/documentation) in the final ZIP package.

---

## 📧 Support
For technical support or license inquiries, contact:
**Support Team:** support@commlink.com.bd
**Website:** [www.commlink.com.bd](https://www.commlink.com.bd)
