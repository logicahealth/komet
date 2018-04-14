/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sh.isaac.komet.flags;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * These images are kept is separate module since they have a different license agreement. 
 * @author kec
 */
public enum CountryFlagImages {
    USA(setupImage("/sh/isaac/komet/flags/united-states-of-america.png")),
    UK(setupImage("/sh/isaac/komet/flags/united-kingdom.png"));
    

    private final Image image;

    private CountryFlagImages(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return this.image;
    }

    public ImageView createImageView() {
        return new ImageView(image);
    }

    public ImageView createImageView(double size) {
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    private static Image setupImage(String imageUrl) {
        return new Image(imageUrl);
    }
}
