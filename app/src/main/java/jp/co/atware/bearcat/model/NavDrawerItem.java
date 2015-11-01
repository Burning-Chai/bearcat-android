package jp.co.atware.bearcat.model;

public class NavDrawerItem {

    private String title;
    private int icon;

    public NavDrawerItem(final String title) {
        this.title = title;
    }

    public NavDrawerItem(final String title, final int isIconResourceId) {
        this.title = title;
        this.icon = isIconResourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }
}
