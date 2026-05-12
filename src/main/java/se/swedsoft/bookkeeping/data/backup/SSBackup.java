package se.swedsoft.bookkeeping.data.backup;


import se.swedsoft.bookkeeping.data.backup.util.SSBackupType;
import se.swedsoft.bookkeeping.util.SSDateUtil;

import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream.PutField;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * Date: 2006-mar-03
 * Time: 09:03:45
 */
public class SSBackup implements Serializable {

    static final long serialVersionUID = 1L;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("iFilename", String.class),
            new ObjectStreamField("iDate", Object.class),
            new ObjectStreamField("iType", SSBackupType.class)
    };

    // The filename of the backup
    private String      iFilename;

    // The date of the backup
    private LocalDateTime iDate;

    // The type of the backup
    private SSBackupType iType;

    /**
     *
     * @param pType
     */
    public SSBackup(SSBackupType pType) {
        iType = pType;
    }

    /**
     *
     * @return the filename
     */
    public String getFilename() {
        return iFilename;
    }

    /**
     *
     * @param iFilename
     */
    public void setFilename(String iFilename) {
        this.iFilename = iFilename;
    }

    // ///////////////////////////////////////////////////////////////////

    public LocalDateTime getLocalDateTime() {
        return iDate;
    }

    public void setLocalDateTime(LocalDateTime iDate) {
        this.iDate = iDate;
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     *
     * @return the type
     */
    public SSBackupType getType() {
        return iType;
    }

    /**
     *
     * @param iType
     */
    public void setType(SSBackupType iType) {
        this.iType = iType;
    }

    // ///////////////////////////////////////////////////////////////////

    // ///////////////////////////////////////////////////////////////////

    /**
     * Removes the backup from disk
     */
    public void delete() {
        File iFile = new File(iFilename);

        if (iFile.exists()) {
            iFile.delete();
        }
    }

    /**
     *
     * @return if the backup exists
     */
    public boolean exists() {
        File iFile = new File(iFilename);

        return iFile.exists();
    }

    /**
     *
     * @param iFile
     * @return the backup
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static SSBackup loadBackup(File iFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream iObjectInputStream = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(iFile)))) {
            return (SSBackup) iObjectInputStream.readObject();
        }
    }

    /**
     *
     * @param iFile
     * @param iBackup
     * @throws IOException
     */
    public static void storeBackup(File iFile, SSBackup iBackup) throws IOException {
        try (ObjectOutputStream iObjectOutputStream = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(iFile)))) {
            iObjectOutputStream.writeObject(iBackup);
        }
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        PutField fields = outputStream.putFields();

        fields.put("iFilename", iFilename);
        fields.put("iDate", iDate);
        fields.put("iType", iType);

        outputStream.writeFields();
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        GetField fields = inputStream.readFields();

        iFilename = (String) fields.get("iFilename", null);
        iType = (SSBackupType) fields.get("iType", null);

        Object rawDate = fields.get("iDate", null);
        if (rawDate instanceof LocalDateTime) {
            iDate = (LocalDateTime) rawDate;
        } else if (rawDate == null || rawDate instanceof Date) {
            iDate = SSDateUtil.toLocalDateTime((Date) rawDate);
        } else {
            throw new InvalidObjectException("Unsupported backup date type: " + rawDate.getClass().getName());
        }
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(iFilename);
        sb.append(", ");
        sb.append(iDate);
        sb.append(", ");
        sb.append(iType);

        return super.toString();
    }

}
