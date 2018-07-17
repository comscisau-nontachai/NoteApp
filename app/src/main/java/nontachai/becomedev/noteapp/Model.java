package nontachai.becomedev.noteapp;

public class Model {

    public String mId, mTitle, mDesc,mDate;

    public Model() {

    }

    public Model(String mId, String mTitle, String mDesc,String mDate) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mDesc = mDesc;
        this.mDate = mDate;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmDesc() {
        return mDesc;
    }

    public void setmDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }
}
