package Graphics;
import java.awt.Color;

public class Screen {
	public int[][] map;
	public int mapWidth, mapHeight, width, height;

	// constructor
	public Screen(int[][] m, int w, int h) {
		map = m;
		width = w;
		height = h;
	}

	// recalculates how the screen should look to the user based on their position in the map
	// returns the updated array of pixels to the Game class
	public int[] update (Player camera, int[] pixels) {

		// clear screen
		for (int n = 0; n < pixels.length / 2; n++){
			if(pixels[n] != Color.DARK_GRAY.getRGB()) pixels[n] = Color.DARK_GRAY.getRGB();
		}
		for (int i = pixels.length / 2; i < pixels.length; i++){
			if(pixels[i] != Color.gray.getRGB()) pixels[i] = Color.gray.getRGB();
		}

		// loops through every vertical bar on the screen and casts a ray 
		// to figure out what wall should be on the screen at that vertical bar
		for (int x = 0; x < width; x++){
			double cameraX = 2 * x / (double)(width) - 1; // x-coordinate of the current vertical stripe on the camera plane

			// make a vector for the ray
			double rayDirX = camera.getXDir() + camera.getXPlane() * cameraX;
			double rayDirY = camera.getYDir() + camera.getYPlane() * cameraX;

			// Map position
			int mapX = (int) camera.getXPos();
			int mapY = (int) camera.getYPos();

			// length of ray from current position to next x or y-side
			double sideDistX;
			double sideDistY;

			// Length of ray from one side to next in map
			double deltaDistX = Math.sqrt(1 + (rayDirY*rayDirY) / (rayDirX*rayDirX));
			double deltaDistY = Math.sqrt(1 + (rayDirX*rayDirX) / (rayDirY*rayDirY));
			double perpWallDist; // distance from the player to the first wall the ray collides with

			// Direction to go in x and y
			int stepX, stepY;
			boolean hit = false; // was a wall hit
			int side=0; // was the wall vertical or horizontal
			
			//Figure out the step direction and initial distance to a side
			if (rayDirX < 0){
			    stepX = -1;
			    sideDistX = (camera.getXPos() - mapX) * deltaDistX;
			} else {
			    stepX = 1;
			    sideDistX = (mapX + 1.0 - camera.getXPos()) * deltaDistX;
			}
			
			if (rayDirY < 0){
			    stepY = -1;
			    sideDistY = (camera.getYPos() - mapY) * deltaDistY;
			} else {
			    stepY = 1;
			    sideDistY = (mapY + 1.0 - camera.getYPos()) * deltaDistY;
			}
			
			// Loop to find where the ray hits a wall
			while(!hit) {
			    // Jump to next square
			    if (sideDistX < sideDistY){
			        sideDistX += deltaDistX;
			        mapX += stepX;
			        side = 0;
			    } else {
			        sideDistY += deltaDistY;
			        mapY += stepY;
			        side = 1;
			    }
			    
			    // Check if ray has hit a wall
			    if(map[mapX][mapY] > 0) hit = true;
			}
			
			//Calculate distance to the point of impact
			if(side == 0){
				perpWallDist = Math.abs((mapX - camera.getXPos() + (1 - stepX) / 2) / rayDirX);
			} else {
				perpWallDist = Math.abs((mapY - camera.getYPos() + (1 - stepY) / 2) / rayDirY);    
			}
			
			//Now calculate the height of the wall based on the distance from the camera
			int lineHeight;
			if(perpWallDist > 0) lineHeight = Math.abs((int)(height / perpWallDist));
			else lineHeight = height;
			
			//calculate lowest and highest pixel to fill in current stripe
			int drawStart = -lineHeight / 2 + height / 2;
			if (drawStart < 0){
				drawStart = 0;
			}
			
			int drawEnd = lineHeight/2 + height/2;
			if (drawEnd >= height){
				drawEnd = height - 1;
			}
			
			// add a texture
			// int texNum = map[mapX][mapY] - 1;
			double wallX; // Exact position of where wall was hit
			if(side == 1) { // If its a y-axis wall
			    wallX = (camera.getXPos() + ((mapY - camera.getYPos() + (1 - stepY) / 2) / rayDirY) * rayDirX);
			} else { // X-axis wall
			    wallX = (camera.getYPos() + ((mapX - camera.getXPos() + (1 - stepX) / 2) / rayDirX) * rayDirY);
			}
			wallX -= Math.floor(wallX);
			
			// x coordinate on the texture
			int texX = (int)(wallX * (64));
			if(side == 0 && rayDirX > 0) texX = 64 - texX - 1;
			if(side == 1 && rayDirY < 0) texX = 64 - texX - 1;
			
			// calculate y coordinate on texture
			for(int y = drawStart; y < drawEnd; y++) {
			    // int texY = (((y*2 - height + lineHeight) << 6) / lineHeight) / 2;
			    int color;
			    color = Color.CYAN.getRGB();// + (int)(Math.random() * 10 - 5);
//			    if(side==0) color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
//			    else color = (textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)]>>1) & 8355711;//Make y sides darker
			    pixels[x + y * width] = color;
			}
		}

		return pixels;
	}
}
