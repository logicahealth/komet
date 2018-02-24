/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.dbConfigBuilder.fx.fxUtil;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * Constants for images in the GUI
 */
public enum Images
{
	EXCLAMATION(setupImage("/images/fugue/16x16/icons-shadowless/exclamation-red.png")),
	INFORMATION(setupImage("/images/fugue/16x16/icons-shadowless/information.png")),
	PACKAGE(setupImage("/images/silk-icons/src/main/resources/silk/16x16/package_green.png"));

	private final Image image;

	private Images(Image image)
	{
		this.image = image;
	}

	public Image getImage()
	{
		return this.image;
	}

	public ImageView createImageView()
	{
		return new ImageView(image);
	}

	private static Image setupImage(String imageUrl)
	{
		return new Image(imageUrl);
	}
}
