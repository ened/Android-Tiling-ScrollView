package asia.ivity.android.tiledscrollview;

/**
* Configures the ScrollView. A configuration set is depending on the zoom level it's in.
*
* @author Sebastian Roth <sebastian.roth@gmail.com>
*/
public class ConfigurationSet {
    String filePattern;
    int tileWidth;
    int tileHeight;
    int imageWidth;
    int imageHeight;

    public ConfigurationSet(String filePattern, int tileWidth, int tileHeight, int imageWidth, int imageHeight) {
        this.filePattern = filePattern;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }
}
