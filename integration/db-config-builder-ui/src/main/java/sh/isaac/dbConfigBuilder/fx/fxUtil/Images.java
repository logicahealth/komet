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

//TODO if we end up keeping any of the views that use these, merge with Iconography

public enum Images
{
	EXCLAMATION(setupImage("/images/fugue/16x16/icons-shadowless/exclamation-red.png")),
	INFORMATION(setupImage("/images/fugue/16x16/icons-shadowless/information.png")),
	PACKAGE(setupImage("/images/silk/16x16/package_green.png")),
	COPY(setupImage("/icons/fugue/16x16/icons-shadowless/document-copy.png")),
	SEARCH(setupImage("/icons/fugue/16x16/icons-shadowless/application-search-result.png")),
	FILTER_16(setupImage("/icons/misc/16x16/filter.png")),
	HISTORICAL(setupImage("/icons/fugue/16x16/icons-shadowless/clock-history.png")),
	BLACK_DOT(setupImage("/icons/diagona/16x16/158.png")),
	GREY_DOT(setupImage("/icons/diagona/16x16/159.png")),
	YELLOW_DOT(setupImage("/icons/diagona/16x16/154.png")),
	MINUS(setupImage("/icons/fugue/16x16/icons-shadowless/minus.png")), 
	PLUS(setupImage("/icons/fugue/16x16/icons-shadowless/plus.png")),
	STAMP(setupImage("/icons/fugue/16x16/icons-shadowless/stamp-medium.png")),
	EDIT(setupImage("/icons/silk/16x16/pencil.png")),
	HISTORY(setupImage("/icons/fugue/16x16/icons-shadowless/clock.png")),
	DISPLAY_FSN(setupImage("/icons/wb-icons/16x16/plain/truck_red.png")),
	DISPLAY_PREFERRED(setupImage("/icons/wb-icons/16x16/plain/car_compact_green.png"));


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
