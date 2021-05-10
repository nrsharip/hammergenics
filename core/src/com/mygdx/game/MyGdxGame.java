package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;


public class MyGdxGame extends ApplicationAdapter {
	private OrthographicCamera camera;
	private SpriteBatch batch;

	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;

	private Rectangle bucket;
	private Array<Rectangle> raindrops;

	private long lastDropTime;

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800-64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}


	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("drop.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		// you should use
		// - a Sound instance if your sample is shorter than 10 seconds, and
		// - a Music instance for longer audio pieces.
		dropSound = Gdx.audio.newSound(Gdx.files.internal("30341__junggle__waterdrop24.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("28283__acclivity__undertreeinrain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = 800 / 2 - 64 / 2;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();

		// ... more to come ...
	}

	@Override
	public void render() {
		// ScreenUtils.clear(r, g, b, a) are the red, green, blue and alpha component of that color,
		// each within the range [0, 1]
		ScreenUtils.clear(0, 0, 0.2f, 1);

		camera.update();

		// tell the SpriteBatch to use the coordinate system specified by the camera
		// camera.combined field is a matrix
		batch.setProjectionMatrix(camera.combined);

		// OpenGL hates nothing more than telling it about individual images.
		// It wants to be told about as many images to render as possible at once.
		// SpriteBatch records all drawing commands in between SpriteBatch.begin() and SpriteBatch.end().
		// After SpriteBatch.end() it will submit all drawing requests we made at once, speeding up rendering quite a bit
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();

			// OrthographicCamera is actually a 3D camera which takes into account z-coordinates as well
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			//transform the touch/mouse coordinates to our camera’s coordinate system
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}

		// two hundred pixels/units per second
		// Gdx.graphics.getDeltaTime() returns the time passed between the last and the current frame in seconds
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 800 - 64;

		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) iter.remove();

			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}

      //... more to come here ...
	}
	
	@Override
	public void dispose () {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
