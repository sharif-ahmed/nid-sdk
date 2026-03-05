package citl_nid_sdk.parser.models;

/**
 * POJO for holding extracted NID data.
 */
public class NidData {
    private String nameBn = "";
    private String nameEn = "";
    private String fatherName = "";
    private String motherName = "";
    private String dateOfBirth = "";
    private String nidNumber = "";
    private boolean isValid = false;
    private String cardType = "";

    // Getters and Setters
    public String getNameBn() { return nameBn; }
    public void setNameBn(String nameBn) { this.nameBn = nameBn; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNidNumber() { return nidNumber; }
    public void setNidNumber(String nidNumber) { this.nidNumber = nidNumber; }

    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { isValid = valid; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    @Override
    public String toString() {
        return "NidData{" +
                "nameBn='" + nameBn + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", fatherName='" + fatherName + '\'' +
                ", motherName='" + motherName + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", nidNumber='" + nidNumber + '\'' +
                ", isValid=" + isValid +
                ", cardType='" + cardType + '\'' +
                '}';
    }
}
