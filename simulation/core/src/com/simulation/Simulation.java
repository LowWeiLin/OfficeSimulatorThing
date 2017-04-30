package com.simulation;

import static com.badlogic.gdx.graphics.GL20.GL_NEAREST;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_MAG_FILTER;

import java.util.Arrays;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

public class Simulation extends ApplicationAdapter {

  private PerspectiveCamera camera;
  private CameraInputController cameraInputController;
  private Environment environment;
  private ModelBatch modelBatch;

  private Model model;
  private ModelInstance modelInstance;
  private Model groundModel;
  private ModelInstance groundModelInstance;

  public Simulation() {
  }

  @Override
  public void create() {

    modelBatch = new ModelBatch();

    cameraInputController = new CameraInputController(null);
    Gdx.input.setInputProcessor(cameraInputController);

    try {
      loadModels();
    } catch (GdxRuntimeException e) {
      e.printStackTrace();
    }

    environment = new Environment();
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
    environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

    final Color backgroundColor = new Color(100 / 255f, 149 / 255f, 237 / 255f, 1f);
    Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
  }

  @Override
  public void render() {

    cameraInputController.update();

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    // Multisample antialiasing
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT |
      (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

    // Disable bilinear filtering on textures
    Gdx.gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    if (modelInstance != null) {
      modelBatch.begin(camera);
      modelBatch.render(Arrays.asList(groundModelInstance, modelInstance), environment);
      modelBatch.end();
    }
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    resetCamera();
  }

  @Override
  public void dispose() {
    modelBatch.dispose();
    model.dispose();
    groundModel.dispose();
  }

  private void loadModels() throws GdxRuntimeException {
    ModelBuilder mb = new ModelBuilder();
    groundModel = mb.createBox(100f, 0.1f, 100f,
      new Material(ColorAttribute.createDiffuse(new Color(0xdfdfdfff))),
      VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

    String path = "data/chr_scientist.g3dj";
    if (path.toLowerCase().endsWith("obj")) {
      ObjLoader objLoader = new ObjLoader();
      model = objLoader.loadModel(Gdx.files.internal(path));
    } else if (path.toLowerCase().endsWith("g3dj")) {
      G3dModelLoader g3djModelLoader = new G3dModelLoader(new JsonReader());
      model = g3djModelLoader.loadModel(Gdx.files.internal(path));
    } else if (path.toLowerCase().endsWith("g3db")) {
      G3dModelLoader g3dbModelLoader = new G3dModelLoader(new UBJsonReader());
      model = g3dbModelLoader.loadModel(Gdx.files.internal(path));
    } else {
      throw new RuntimeException("unknown model format " + path);
    }

    modelInstance = new ModelInstance(model);
    groundModelInstance = new ModelInstance(groundModel);

    // Put it slightly under the unit's feet
    groundModelInstance.transform.translate(0f, -0.1f, 0f);

    setBackFaceCulling(modelInstance, true);
    setAlphaBlending(modelInstance, false);
    setAlphaTest(modelInstance, -1);
  }

  private void resetCamera() {
    float offset = 4f;
    camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.position.set(offset, offset, offset);
    camera.lookAt(0, 0, 0);
    camera.near = 1f;
    camera.far = 300f;
    camera.update();
    cameraInputController.camera = camera;
    cameraInputController.update();
  }

  private static void setBackFaceCulling(ModelInstance modelInstance, boolean enabled) {
    if (enabled) {
      for (Material mat : modelInstance.materials) {
        mat.remove(IntAttribute.CullFace);
      }
    } else {
      for (Material mat : modelInstance.materials) {
        mat.set(new IntAttribute(IntAttribute.CullFace, 0));
      }
    }
  }

  private static void setAlphaBlending(ModelInstance modelInstance, boolean enabled) {
    if (enabled) {
      for (Material mat : modelInstance.materials) {
        mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
      }
    } else {
      for (Material mat : modelInstance.materials) {
        mat.remove(BlendingAttribute.Type);
      }
    }
  }

  private static void setAlphaTest(ModelInstance modelInstance, float value) {
    if (value >= 0) {
      for (Material mat : modelInstance.materials) {
        mat.set(new FloatAttribute(FloatAttribute.AlphaTest, value));
      }
    } else {
      for (Material mat : modelInstance.materials) {
        mat.remove(FloatAttribute.AlphaTest);
      }
    }
  }
}
