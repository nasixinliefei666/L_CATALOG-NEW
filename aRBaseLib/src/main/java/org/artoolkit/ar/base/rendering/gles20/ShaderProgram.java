package org.artoolkit.ar.base.rendering.gles20;

import android.opengl.GLES20;

import org.artoolkit.ar.base.rendering.RenderUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by Thorsten Bux on 21.01.2016.
 * <p/>
 * The shader program links together the vertex shader and the fragment shader and compiles them.
 * It also is responsible for binding the attributes. Attributes can be used to pass in values to the
 * shader during runtime.
 * <p/>
 * It is important to call {@link #setupShaderUsage()} as first method inside your
 * implementation of the {@link #render(float[])} render()} method.
 * <p/>
 * This abstract class provides the basic implementation for binding shaders see {@link #createProgram(int, int)}
 * you can just call this method and do not need to worry about binding shaders.
 * <p/>
 * This class also provides stubs of methodes you might want to override when you create your own Shader Program.
 * You can see an example Shader Program in {@link BaseShaderProgram}
 * <p/>
 * Finally it renders the given geometry.
 */
public abstract class ShaderProgram {

    /* Size of the position data in elements. */
    protected final int positionDataSize = 3;

    //Size of color data in elements
    protected final int colorDataSize = 4;
    /* How many bytes per float. */
    protected final int mBytesPerFloat = Float.SIZE / 8;


    /* How many elements per vertex in bytes*/
    protected final int positionStrideBytes = positionDataSize * mBytesPerFloat;

    /* How many elements per vertex in bytes for the color*/
    protected final int colorStrideBytes = colorDataSize * mBytesPerFloat;

    protected final int shaderProgramHandle;
    protected float[] projectionMatrix;
    protected float[] modelViewMatrix;

    public ShaderProgram(OpenGLShader vertexShader, OpenGLShader fragmentShader) {
        shaderProgramHandle = createProgram(vertexShader.configureShader(), fragmentShader.configureShader());
    }

    public abstract int getProjectionMatrixHandle();

    public abstract int getModelViewMatrixHandle();

    protected abstract void bindAttributes();

    private int createProgram(int vertexShaderHandle, int fragmentShaderHandle) {
        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();
        String programErrorLog = "";

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                programErrorLog = GLES20.glGetProgramInfoLog(programHandle);
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.\\n " + programErrorLog);
        }
        return programHandle;
    }

    public int getShaderProgramHandle() {
        return shaderProgramHandle;
    }

    /**
     * Full loaded render function. You should at least override this one.
     * You need to set the projection and/or modelview matrix befor calling a render method.
     *
     * @param vertexBuffer position vertex information
     * @param colorBuffer  color information
     * @param indexBuffer  index
     */
    public void render(FloatBuffer vertexBuffer, FloatBuffer colorBuffer, ByteBuffer indexBuffer) {
        throw new RuntimeException("Please override at least this method.");
    }

    public void render(FloatBuffer vertexBuffer, ByteBuffer indexBuffer) {
        render(vertexBuffer, null, indexBuffer);
    }

    /**
     * Only render a simple position. In this case the implementation if forwarded to the
     * {@link #render(FloatBuffer, FloatBuffer, ByteBuffer)} but you can override this one directly
     * as shown in {@link BaseShaderProgram}
     *
     * @param position The position to be rendered
     */
    public void render(float[] position) {
        render(RenderUtils.buildFloatBuffer(position), null);
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void setModelViewMatrix(float[] modelViewMatrix) {
        this.modelViewMatrix = modelViewMatrix;
    }

    /**
     * Sets some basic settings for shader usage.
     * Needs to be called as first method from your implementation inside the
     * {@link #render(FloatBuffer, FloatBuffer, ByteBuffer) renderer()} method.
     */
    protected void setupShaderUsage() {
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(shaderProgramHandle);

        /* Replaces the functions

                Apply the ARToolKit projection matrix
                GLES10.glMatrixMode(GL10.GL_PROJECTION);
                GLES10.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);

                gl.glMatrixMode(GL10.GL_MODELVIEW);
                gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(markerID), 0);

           from the Renderer implementation class in the render method
           */

        if (projectionMatrix != null)
            GLES20.glUniformMatrix4fv(this.getProjectionMatrixHandle(), 1, false, projectionMatrix, 0);
        else
            throw new RuntimeException("You need to set the projection matrix.");

        if (modelViewMatrix != null)
            GLES20.glUniformMatrix4fv(this.getModelViewMatrixHandle(), 1, false, modelViewMatrix, 0);
    }
}
