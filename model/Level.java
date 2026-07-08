package HollowKnight.hollowknight.model;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level {
    private static final float SCALE = 4f;

    private static final String[] BG_LAYER_NAMES = { "FarBackground", "Background", "Terrain" };
    private static final String[] FG_LAYER_NAMES = { "Foreground" };

    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    private final List<Rectangle> platforms = new ArrayList<>();
    private final List<Rectangle> spikes = new ArrayList<>();
    private final List<Rectangle> acid = new ArrayList<>();
    private final List<Rectangle> portalBounds = new ArrayList<>();
    private final List<String> portalTargets = new ArrayList<>();
    private Rectangle waterfall = null;

    private final List<Vector2> mossflySpawns = new ArrayList<>();
    private final List<Vector2> crawlerSpawns = new ArrayList<>();
    private final List<Vector2> hornheadSpawns = new ArrayList<>();
    private final List<Vector2> guardianSpawns = new ArrayList<>();

    private final float width, height;
    private final Vector2 playerSpawn = new Vector2(150f, 200f);
    private final Vector2 lastSafePosition = new Vector2(150f, 200f);
    private final Vector2 zoteSpawn = new Vector2(4600f, 2360f);
    private final Vector2 falseKnightSpawn = new Vector2();
    private Rectangle arena = null;

    private final List<BreakableWall> breakableWalls = new ArrayList<>();
    public List<BreakableWall> getBreakableWalls() { return breakableWalls; }

    private int[] bgLayers;
    private int[] fgLayers;

    public Level(String tmxFilePath) {
        this.map = new TmxMapLoader().load(tmxFilePath);
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, SCALE);

        int wTiles = map.getProperties().get("width", Integer.class);
        int hTiles = map.getProperties().get("height", Integer.class);
        int tw = map.getProperties().get("tilewidth", Integer.class);
        int th = map.getProperties().get("tileheight", Integer.class);
        this.width = wTiles * tw * SCALE;
        this.height = hTiles * th * SCALE;

        parseMapObjects();
        cacheLayers();
    }

    private void cacheLayers() {
        bgLayers = resolveLayers(BG_LAYER_NAMES);
        fgLayers = resolveLayers(FG_LAYER_NAMES);
    }

    private int[] resolveLayers(String[] names) {
        int[] found = new int[names.length];
        int count = 0;
        for (String name : names) {
            int idx = map.getLayers().getIndex(name);
            if (idx >= 0) found[count++] = idx;
        }
        return Arrays.copyOf(found, count);
    }

    public void parseMapObjects() {
        MapLayer objectLayer = findObjectLayer();
        if (objectLayer == null) return;

        for (MapObject object : objectLayer.getObjects()) {
            String name = object.getName();

            if (name != null && name.startsWith("spawn")) {
                float sx = object.getProperties().get("x", Float.class) * SCALE;
                float sy = object.getProperties().get("y", Float.class) * SCALE;
                registerSpawn(name, sx, sy);
                continue;
            }

            if (!(object instanceof RectangleMapObject)) continue;
            Rectangle r = ((RectangleMapObject) object).getRectangle();
            Rectangle scaled = new Rectangle(r.x * SCALE, r.y * SCALE, r.width * SCALE, r.height * SCALE);

            if ("spike".equals(name)) spikes.add(scaled);
            else if ("acid".equals(name)) acid.add(scaled);
            else if ("waterfall".equals(name)) waterfall = scaled;
            else if ("breakable".equals(name)) breakableWalls.add(
                new BreakableWall(scaled.x, scaled.y, scaled.width, scaled.height));
            else if ("arena".equals(name)) arena = scaled;
            else if ("portal".equals(name)) {
                portalBounds.add(scaled);
                portalTargets.add(object.getProperties().get("target", "", String.class));
            }
            else platforms.add(scaled);
        }
    }

    private MapLayer findObjectLayer() {
        for (MapLayer layer : map.getLayers()) {
            if (layer.getName().equals("Objects") || layer.getName().equals("Collision")) return layer;
        }
        System.err.println("No object layer found.");
        return null;
    }

    private void registerSpawn(String name, float sx, float sy) {
        if (name.equals("spawnKnight") || name.equals("spawn")) {
            playerSpawn.set(sx, sy);
            updateSafePosition(sx, sy);
        } else if (name.equals("spawnZote")) {
            zoteSpawn.set(sx, sy);
        } else if (name.equals("spawnFalseKnight")) {
            falseKnightSpawn.set(sx, sy);
        } else if (name.startsWith("spawnMossfly")) {
            mossflySpawns.add(new Vector2(sx, sy));
        } else if (name.startsWith("spawnCrawler")) {
            crawlerSpawns.add(new Vector2(sx, sy));
        } else if (name.startsWith("spawnHornhead")) {
            hornheadSpawns.add(new Vector2(sx, sy));
        } else if (name.startsWith("spawnGuardian") || name.startsWith("spawnCrystal")) {
            guardianSpawns.add(new Vector2(sx, sy));
        }
    }

    public void hideTilesIn(Rectangle world) {
        int tw = map.getProperties().get("tilewidth", Integer.class);
        int th = map.getProperties().get("tileheight", Integer.class);
        float tileW = tw * SCALE, tileH = th * SCALE;
        int colStart = (int) Math.floor(world.x / tileW);
        int colEnd   = (int) Math.floor((world.x + world.width - 1f) / tileW);
        int rowStart = (int) Math.floor(world.y / tileH);
        int rowEnd   = (int) Math.floor((world.y + world.height - 1f) / tileH);

        for (MapLayer layer : map.getLayers()) {
            if (!(layer instanceof TiledMapTileLayer)) continue;
            TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
            for (int col = colStart; col <= colEnd; col++) {
                for (int row = rowStart; row <= rowEnd; row++) {
                    if (col >= 0 && row >= 0 && col < tileLayer.getWidth() && row < tileLayer.getHeight()) {
                        tileLayer.setCell(col, row, null);
                    }
                }
            }
        }
    }

    public void renderBackground(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render(bgLayers);
    }

    public void renderForeground(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render(fgLayers);
    }

    public List<Rectangle> getPlatforms() { return platforms; }
    public List<Rectangle> getSpikes() { return spikes; }
    public List<Rectangle> getAcid() { return acid; }

    public int portalIndexAt(Rectangle box) {
        for (int i = 0; i < portalBounds.size(); i++) {
            if (portalBounds.get(i).overlaps(box)) return i;
        }
        return -1;
    }

    public String getPortalTarget(int i) { return portalTargets.get(i); }

    public Rectangle getWaterfall() { return waterfall; }
    public Vector2 getPlayerSpawn() { return playerSpawn; }
    public Vector2 getZoteSpawn() { return zoteSpawn; }
    public Vector2 getFalseKnightSpawn() { return falseKnightSpawn; }
    public Rectangle getArena() { return arena; }
    public boolean hasBoss() { return arena != null; }
    public List<Vector2> getMossflySpawns() { return mossflySpawns; }
    public List<Vector2> getCrawlerSpawns() { return crawlerSpawns; }
    public List<Vector2> getHornheadSpawns() { return hornheadSpawns; }
    public List<Vector2> getGuardianSpawns() { return guardianSpawns; }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public Vector2 getLastSafePosition() { return lastSafePosition; }
    public void updateSafePosition(float x, float y) { lastSafePosition.set(x, y); }

    public void dispose() {
        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
    }
}