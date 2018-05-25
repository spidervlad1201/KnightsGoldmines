package com.vakuor.knightsandgoldmines.utilities;

/**
 * Author: Luca "Burn" Vignaroli
 * luca@burning.it
 * Date: 10/12/12
 * Time: 20.28
 * License: Apache 2 (http://www.apache.org/licenses/LICENSE-2.0.html)
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.Arrays;

public class MapRayCaster implements Disposable {
    public static final String NAME = MapRayCaster.class.getSimpleName();

    // Local variables
    private MapManager _mapManager;
    public Vector2[] _raycastBuffer;
    private int _raycastBufferMaxLength;
    private Vector2 _rayStartPoint;
    private Vector2 _rayEndPoint;
    private float _rayLength;
    private float[] _result;
    public int _tileWidth;
    public int _tileHeight;
    private int _viewportWidth;
    private int _viewportHeight;

    // Bresenham line related
    int _deltax;
    int _deltay;
    int _bresenhamError;
    int _ystep;
    int _y, _x;

    // Various
    private Vector2 _collisionPoint;
    private float _collisionLength;
    public boolean _rayInvertedPoints;
    private int _collidableTiles[];
    private int _collidableTilesCount;

    // Tile lookup
    public int _rayTilesFirstColumn;
    public int _rayTilesFirstRow;
    public int _rayTilesColumns;
    public int _rayTilesRows;

    // Camera related
    private OrthographicCamera _camera;
    private Matrix4 _worldProjection = new Matrix4();
    private Matrix4 _screenProjection = new Matrix4();

    // Rendering
    private ShapeRenderer _shapeRenderer;

    /**
     * Initialize a raycaster that can return a maximum amount of points between point A and point B.
     *
     */
    public MapRayCaster(MapManager mapManager) {
        // Init _map and constant geometries
        _mapManager = mapManager;
        _tileWidth = mapManager._tiledMap.tileWidth;
        _tileHeight = mapManager._tiledMap.tileHeight;
        _collisionPoint = new Vector2(0, 0);
        _rayStartPoint = new Vector2(0, 0);
        _rayEndPoint = new Vector2(0, 0);
        _shapeRenderer = new ShapeRenderer();

        // Store our matrices for both world and screen projection
        _camera = mapManager._camera;
        _worldProjection = _camera.combined;
        _screenProjection.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialize ray buffer
        _viewportWidth = (int) mapManager._camera.viewportWidth;
        _viewportHeight = (int) mapManager._camera.viewportHeight;

        // Initialize raycaster and collision related vars
        _raycastBufferMaxLength = (int) Math.ceil(Math.sqrt(_viewportWidth * _viewportWidth + _viewportHeight * _viewportHeight));
        _raycastBuffer = new Vector2[_raycastBufferMaxLength];
        Arrays.fill(_raycastBuffer, new Vector2(0,0));

        _result = new float[3];
        _collidableTiles = new int[_mapManager.mapNumRows * _mapManager.mapNumCols];

        Gdx.app.log(NAME, "Initialized with a max line length of " + _raycastBufferMaxLength + " points");
    }

    /**
     * Cast the ray and return the result
     *
     * @param start Ray start point
     * @param end Ray end point
     * @return float[] Result of the raycast
     *
     * result[0]: collision X
     * result[1]: collision Y
     * result[2]: Result -1: Exceeded max length
     *            Result between 0 and 1: collision point length from the start point
     */
    public float[] castRay(Vector2 start, Vector2 end) {
        // Register the start and end points
        _rayStartPoint = start;
        _rayEndPoint = end;

        // Get the metrics of the region of tiles affected by this ray
        findCollidableTiles();

        // The target is out of reach, no collision
        if (start.dst(end) > _raycastBufferMaxLength) {
            // Assemble the result
            _result[0] = _rayEndPoint.x;
            _result[1] = _rayEndPoint.y;
            _result[2] = -1;
        } else {
            // Get all the points along the ray
            bresenhamLine(_rayStartPoint, _rayEndPoint);

            // Search for collisions
            computeCollision();

            // Assemble the result
            _result[0] = _collisionPoint.x;
            _result[1] = _collisionPoint.y;
            _result[2] = _collisionLength / _rayLength;
        }

        // Render debug info
        //renderRayDebug(_rayStartPoint, _rayEndPoint, _result);

        // Create new result and return it
        float[] result = new float[3];
        result[0] = _result[0];
        result[1] = _result[1];
        result[2] = _result[2];

        return result;
    }

    /**
     * Cast the ray and return the result
     *
     * @param start Ray start point
     * @param direction Ray direction vector
     * @param magnitude Ray length
     * @return float[]
     *
     * result[0]: collision X
     * result[1]: collision Y
     * result[2]: -1: ignore this ray and continue  - 0: terminate the ray cast - 0.n and 1: collision point length from the start point - 1: don't clip the ray and continue
     */
    public float[] castRay(Vector2 start, Vector2 direction, int magnitude) {
        return castRay(start, start.add(direction.nor().mul(magnitude)));
    }

    /**
     * Calculate the operational area of tiles (in tiles coordinates) that will be affected by this raycast
     * Sets _rayTilesFirstColumn, _rayTilesLastColumn, _rayTilesFirstRow, _rayTilesLastRow and _rayTilesColumns, _rayTilesRows
     */
    public void findCollidableTiles() {
        // Clear the collidables buffer
        _collidableTilesCount = 0;

        // Get num layers
        int _rayTilesLayers = _mapManager._tiledMap.layers.size();

        // Width and height of the ray
        float rayWidth = Math.abs(_rayStartPoint.x - _rayEndPoint.x);
        float rayHeight = Math.abs(_rayStartPoint.y - _rayEndPoint.y);

        // Direction of the ray
        boolean left = _rayStartPoint.x > _rayEndPoint.x;
        boolean up = _rayStartPoint.y > _rayEndPoint.y;

        // First column and horizontal delta inside the tile
        float deltaWidth = _rayStartPoint.x % _tileWidth;
        _rayTilesFirstColumn = (int) Math.floor( (_rayStartPoint.x) / _tileWidth);

        // First row and vertical delta inside the tile
        float deltaHeight = _rayStartPoint.y % _tileHeight;
        _rayTilesFirstRow = (int) Math.floor( _rayStartPoint.y / _tileHeight);

        // Number of columns and rows
        if (left) {
            _rayTilesColumns = (int) Math.ceil((rayWidth + _tileWidth - deltaWidth) / _tileWidth);
        } else {
            _rayTilesColumns = (int) Math.ceil((rayWidth + _tileWidth + deltaWidth) / _tileWidth) - 1;
        }

        if (up) {
            _rayTilesRows = (int) Math.ceil((rayHeight + _tileHeight - deltaHeight) / _tileHeight);
        } else {
            _rayTilesRows = (int) Math.ceil((rayHeight + _tileHeight + deltaHeight) / _tileHeight) -1;
        }

        // Make sure we have at least the tile containing the ray selected
        _rayTilesColumns = Math.max(1, _rayTilesColumns);
        _rayTilesRows = Math.max(1, _rayTilesRows);

        int _rayTilesLastColumn;
        if (left) {
            _rayTilesLastColumn = _rayTilesFirstColumn;
            _rayTilesFirstColumn -= _rayTilesColumns -1;
        } else {
            _rayTilesLastColumn = _rayTilesFirstColumn + _rayTilesColumns;
        }

        int _rayTilesLastRow;
        if (up) {
            _rayTilesLastRow = _rayTilesFirstRow;
            _rayTilesFirstRow -= _rayTilesRows -1;
        } else {
            _rayTilesLastRow = _rayTilesFirstRow + _rayTilesRows;
        }

        //Make sure we're not out of bounds
        _rayTilesFirstColumn = (_rayTilesFirstColumn < 0) ? 0 : _rayTilesFirstColumn;
        _rayTilesFirstRow = (_rayTilesFirstRow < 0) ? 0 : _rayTilesFirstRow;

        // Make sure we are not out of bounds
        _rayTilesLastColumn = (_rayTilesLastColumn > _mapManager.mapNumCols -1) ? _mapManager.mapNumCols -1 : _rayTilesLastColumn;
        _rayTilesLastRow = (_rayTilesLastRow > _mapManager.mapNumRows -1) ? _mapManager.mapNumRows -1 : _rayTilesLastRow;

        // Store colliding tiles
        int _layer;
        for (_layer = 0; _layer < _rayTilesLayers; _layer++) {
            // Skip this layer if it's not flagged for collision checks
            if (_mapManager._tiledMap.layers.get(_layer).properties.get("checkcollisions") != null) {
                int _row;
                for (_row = _rayTilesFirstRow; _row <= _rayTilesLastRow; _row++) {
                    int _col;
                    for (_col = _rayTilesFirstColumn; _col <= _rayTilesLastColumn; _col++) {
                        // Fetch the current tile Id
                        int _tileId = _mapManager._map[_layer][_row][_col];

                        // If the tile is not empty and is marked as a solid store it for collision computation
                        if (_tileId != 0 && _mapManager._tiledMap.getTileProperty(_tileId, "solid") != null) {
                            _collidableTiles[_collidableTilesCount] = _row;
                            _collidableTilesCount++;
                            _collidableTiles[_collidableTilesCount] = _col;
                            _collidableTilesCount++;
                        }
                    }
                }
            }
        }
    }

    /**
     * Implementation of the bresenham line algorithm to generate a list of points between two points
     * @param start The start point
     * @param end The end point
     */
    private void bresenhamLine(Vector2 start, Vector2 end) {
        Arrays.fill(_raycastBuffer, null);

        // Init array index counter
        int _pointCounter = 0;

        // Map coordinates
        int _x0 = (int) start.x;
        int _y0 = (int) start.y;
        int _x1 = (int) end.x;
        int _y1 = (int) end.y;

        boolean _rayIsSteep = Math.abs(_y1 - _y0) > Math.abs(_x1 - _x0);

        int _tmpSwapValue;
        if (_rayIsSteep) {
            // Swap x with _y on the start point swap(x0, _y0);
            _tmpSwapValue = _x0;
            _x0 = _y0;
            _y0 = _tmpSwapValue;

            // Swap x with _y on the end point swap(_x1, _y1);
            _tmpSwapValue = _x1;
            _x1 = _y1;
            _y1 = _tmpSwapValue;
        }
        if (_x0 > _x1) {
            // Swap x between start and end swap(x0, _x1);
            _tmpSwapValue = _x0;
            _x0 = _x1;
            _x1 = _tmpSwapValue;

            // Swap _y between start and end swap(_y0, _y1);
            _tmpSwapValue = _y0;
            _y0 = _y1;
            _y1 = _tmpSwapValue;
        }

        _deltax = _x1 - _x0;
        _deltay = Math.abs(_y1 - _y0);
        _bresenhamError = 0;
        _y = _y0;

        if (_y0 < _y1)
            _ystep = 1;
        else
            _ystep = -1;

        // Time to collect our bounty!
        for (_x = _x0; _x <= _x1; _x++) {
            if (_rayIsSteep) {
                _raycastBuffer[_pointCounter] = new Vector2(_y, _x);
            } else {
                _raycastBuffer[_pointCounter] = new Vector2(_x, _y);
            }

            _pointCounter++;

            _bresenhamError += _deltay;

            if (2 * _bresenhamError >= _deltax) {
                _y += _ystep;
                _bresenhamError -= _deltax;
            }
        }
    }

    /**
     * Compute the collision and populate the result array with the X,Y and Length of the collision point
     */
    private void computeCollision() {
        // Calculate the ray length so we can determine what slice of the tiles are affected by it
        _rayLength = (float) Math.sqrt((_rayEndPoint.x - _rayStartPoint.x)*(_rayEndPoint.x - _rayStartPoint.x) + (_rayEndPoint.y - _rayStartPoint.y)*(_rayEndPoint.y - _rayStartPoint.y));

        // Clamp it if it wants to be larger than the largest possible ray on screen
        if (_rayLength > _raycastBuffer.length) {
            _rayLength = _raycastBuffer.length;
        }

        // Initialize collision to the end of the ray
        _collisionPoint.x = _rayEndPoint.x;
        _collisionPoint.y = _rayEndPoint.y;
        _collisionLength = _rayLength;

        // Find out which point is closer to the ray start point
        float _firstPoint = (float) Math.sqrt((_raycastBuffer[0].x - _rayStartPoint.x) * (_raycastBuffer[0].x - _rayStartPoint.x) + (_raycastBuffer[0].y - _rayStartPoint.y) * (_raycastBuffer[0].y - _rayStartPoint.y));

        // Sample the second point in the array, that's enough to understand the direction of the array
        float _lastPoint;
        if (_raycastBuffer.length >= 2 && _raycastBuffer[1] != null) {
            _lastPoint = (float) Math.sqrt((_raycastBuffer[1].x - _rayStartPoint.x)*(_raycastBuffer[1].x - _rayStartPoint.x) + (_raycastBuffer[1].y - _rayStartPoint.y)*(_raycastBuffer[1].y - _rayStartPoint.y));
        } else {
            _lastPoint = _firstPoint;
        }

        // Set the direction flag
        _rayInvertedPoints = (Math.min(_firstPoint, _lastPoint)) != _firstPoint;

        // Time to find the closest collision point
        int _collidableTile;
        for (_collidableTile = 0; _collidableTile < _collidableTilesCount; _collidableTile += 2) {
            int _rayPointIndex;
            float _newCollisionPointx;
            float _newCollisionPointy;
            float _newCollisionLength;
            int _tilePositionX;
            int _tilePositionY;
            int _tileRightBound;
            int _tileLeftBound;
            int _tileTopBound;
            int _tileBottomBound;

            if (!_rayInvertedPoints) {
                // Bresenham did not invert the points, we should consider the start point as the first in the ray buffer
                // The first collision we find will be the only one we care about
                for (_rayPointIndex = 0; _rayPointIndex < _raycastBuffer.length; _rayPointIndex++) {
                    // Check if this point is inside the current tile
                    _tilePositionX = _collidableTiles[_collidableTile + 1] * _tileWidth; // COL
                    _tilePositionY = _collidableTiles[_collidableTile] * _tileHeight; // ROW

                    // Calculate tile bounds
                    _tileLeftBound = _tilePositionX;
                    _tileRightBound = _tilePositionX + _tileWidth;
                    // Assuming Y going from the top of the screen and down!
                    _tileTopBound = _tilePositionY;
                    _tileBottomBound = _tilePositionY + _tileHeight;

                    if (_raycastBuffer[_rayPointIndex] != null) {
                        // Point inside a rectangle
                        if ( (_raycastBuffer[_rayPointIndex].x >= _tileLeftBound) && (_raycastBuffer[_rayPointIndex].x <= _tileRightBound) && (_raycastBuffer[_rayPointIndex].y >= _tileTopBound) && (_raycastBuffer[_rayPointIndex].y <= _tileBottomBound) ) {

                            // We found a collision point
                            _newCollisionPointx = _raycastBuffer[_rayPointIndex].x;
                            _newCollisionPointy = _raycastBuffer[_rayPointIndex].y;
                            _newCollisionLength = (float) Math.sqrt((_rayStartPoint.x - _newCollisionPointx) * (_rayStartPoint.x - _newCollisionPointx) + (_rayStartPoint.y - _newCollisionPointy) * (_rayStartPoint.y - _newCollisionPointy));

                            // If this new collision is closer to the start point, pick this one
                            if (_newCollisionLength < _collisionLength) {
                                _collisionPoint.x = _newCollisionPointx;
                                _collisionPoint.y = _newCollisionPointy;
                                _collisionLength = _newCollisionLength;
                            }
                        }
                    }
                }
            } else {
                // Bresenham inverted the points, it means that we should start checking for collision from the end of the buffer,
                // and we should check and find which point collides last, that will be our collision point from the logical start of the line.
                int _rayLastPoint = (int) Math.floor(_rayLength);

                // Ray is not steep, consider the first point as the last in the buffer
                for (_rayPointIndex = _rayLastPoint; _rayPointIndex >= 0; _rayPointIndex--) {
                    if (_raycastBuffer[_rayPointIndex] != null) {
                        // Check if this point is inside the current tile
                        _tilePositionX = _collidableTiles[_collidableTile + 1] * _tileWidth; // COL
                        _tilePositionY = _collidableTiles[_collidableTile] * _tileHeight; // ROW

                        // Calculate tile bounds
                        _tileLeftBound = _tilePositionX;
                        _tileRightBound = _tilePositionX + _tileWidth;
                        // Assuming Y going from the top of the screen and down!
                        _tileTopBound = _tilePositionY;
                        _tileBottomBound = _tilePositionY + _tileHeight;

                        if ( (_raycastBuffer[_rayPointIndex].x >= _tileLeftBound) && (_raycastBuffer[_rayPointIndex].x <= _tileRightBound) && (_raycastBuffer[_rayPointIndex].y >= _tileTopBound) && (_raycastBuffer[_rayPointIndex].y <= _tileBottomBound) ) {

                            // We found our collision point let's set the result and get out
                            _newCollisionPointx = _raycastBuffer[_rayPointIndex].x;
                            _newCollisionPointy = _raycastBuffer[_rayPointIndex].y;
                            _newCollisionLength = (float) Math.sqrt((_rayStartPoint.x - _newCollisionPointx) * (_rayStartPoint.x - _newCollisionPointx) + (_rayStartPoint.y - _newCollisionPointy) * (_rayStartPoint.y - _newCollisionPointy));

                            // If this new collision is closer to the start point, pick this one
                            if (_newCollisionLength < _collisionLength) {
                                _collisionPoint.x = _newCollisionPointx;
                                _collisionPoint.y = _newCollisionPointy;
                                _collisionLength = _newCollisionLength;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Draw the ray up until an eventual collision point
     */
    public void renderRayDebug(Vector2 start, Vector2 end, float[] result) {
        // Render line from center to mouse
        _shapeRenderer.setProjectionMatrix(_worldProjection);
        _shapeRenderer.setColor(0, 1, 0, 1);

        _shapeRenderer.begin(ShapeRenderer.ShapeType.Line);


        if (result[2] == 1) {
            // No collision
            _shapeRenderer.setColor(0, 1, 0, 1);
            _shapeRenderer.line(start.x, start.y, result[0], result[1]);
        } else if (result[2] != -1) {
            // Collision vector
            _shapeRenderer.setColor(1, 1, 0, 1);
            _shapeRenderer.line(start.x, start.y, result[0], result[1]);
        } else {
            // result = -1 : Exceeded collision buffer length
            _shapeRenderer.setColor(1, 0, 0, 1);
            _shapeRenderer.line(start.x, start.y, end.x, end.y);
        }

        _shapeRenderer.end();
    }

    /**
     * Resize the ray points buffer when the screen size changes
     */
    public void resize(int width, int height) {
        _raycastBufferMaxLength = (int) Math.ceil(Math.sqrt(width * width + height * height));
        _raycastBuffer = new Vector2[_raycastBufferMaxLength];
        Arrays.fill(_raycastBuffer, null);
        _result = new float[3];

        Gdx.app.log(NAME, "Resized raycast buffer to a max length of " + _raycastBufferMaxLength + " points.");
    }

    @Override
    public void dispose() {
        Gdx.app.log(NAME, "Disposing map raycaster");

        _raycastBuffer = null;
        _result = null;
    }
}