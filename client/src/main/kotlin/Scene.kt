import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL
import org.khronos.webgl.Float32Array
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.Mat4
import kotlin.js.Date

class Scene (
  val gl : WebGL2RenderingContext) : UniformProvider("scene") {

  val vsQuad = Shader(gl, GL.VERTEX_SHADER, "quad-vs.glsl")
  val fstrace = Shader(gl, GL.FRAGMENT_SHADER, "trace-fs.glsl")
  val traceProgram = Program(gl, vsQuad, fstrace)
  val skyCubeTexture = TextureCube(gl,
      "media/posx512.jpg", "media/negx512.jpg",
      "media/posy512.jpg", "media/negy512.jpg",
      "media/posz512.jpg", "media/negz512.jpg"
    )
  val traceMaterial = Material(traceProgram).apply{
    this["envTexture"]?.set( skyCubeTexture )
  }
  val quadGeometry = TexturedQuadGeometry(gl)
  val traceMesh = Mesh(traceMaterial, quadGeometry)

  val camera = PerspectiveCamera()

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  init{
    gl.enable(GL.DEPTH_TEST)
    addComponentsAndGatherUniforms(*Program.all)
  }

  val lights = Array<Light>(1) { Light(it, *Program.all) }
  init{
    lights[0].position.set(1.0f, 1.0f, 1.0f, 0.0f).normalize();
    lights[0].powerDensity.set(1.0f, 1.0f, 0.0f);
  }
  
  val quadrics = Array<Quadric>(8) { Quadric(it) }
  init{
    quadrics[0].surface.set(Quadric.unitSphere)
    quadrics[0].surface.transform(Mat4().scale(1f, 1f, 1f).translate(3f, 1f, 2f))
    quadrics[0].clipper.set(Quadric.unitSlab)
    quadrics[0].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(3f, 1f, 2f))

    quadrics[1].surface.set(Quadric.unitSphere)
    quadrics[1].surface.transform(Mat4().scale(1f, 1f, 1f).translate(5f, 5f, 5f))
    quadrics[1].clipper.set(Quadric.unitSlab)
    quadrics[1].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(5f, 5f, 5f))

    quadrics[2].surface.set(Quadric.unitSphere)
    quadrics[2].surface.transform(Mat4().scale(1f, 1f, 1f).translate(0.3f, 0.1f, 0.6f))
    quadrics[2].clipper.set(Quadric.unitSlab)
    quadrics[2].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(0.3f, 0.1f, 0.6f))

    quadrics[3].surface.set(Quadric.unitSphere)
    quadrics[3].surface.transform(Mat4().scale(1f, 1f, 1f).translate(10f, 15f, 10f))
    quadrics[3].clipper.set(Quadric.unitSlab)
    quadrics[3].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(10f, 15f, 10f))

    quadrics[4].surface.set(Quadric.unitSphere)
    quadrics[4].surface.transform(Mat4().scale(1f, 1f, 1f).translate(1f, 10f, 6f))
    quadrics[4].clipper.set(Quadric.unitSlab)
    quadrics[4].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(1f, 10f, 6f))

    quadrics[5].surface.set(Quadric.unitSphere)
    quadrics[5].surface.transform(Mat4().scale(1f, 1f, 1f).translate(8f, 0f, 10f))
    quadrics[5].clipper.set(Quadric.unitSlab)
    quadrics[5].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(8f, 0f, 10f))

    quadrics[6].surface.set(Quadric.unitSphere)
    quadrics[6].surface.transform(Mat4().scale(1f, 1f, 1f).translate(20f, 1f, 2f))
    quadrics[6].clipper.set(Quadric.unitSlab)
    quadrics[6].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(20f, 1f, 2f))

    quadrics[7].surface.set(Quadric.unitSphere)
    quadrics[7].surface.transform(Mat4().scale(1f, 1f, 1f).translate(3f, 7f, 3f))
    quadrics[7].clipper.set(Quadric.unitSlab)
    quadrics[7].clipper.transform(Mat4().scale(30.0f,30.0f,30.0f).translate(3f, 7f, 3f))
  }

  fun resize(gl : WebGL2RenderingContext, canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)
    camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
  }

  fun update(gl : WebGL2RenderingContext, keysPressed : Set<String>) {

    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t  = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f    
    timeAtLastFrame = timeAtThisFrame

    camera.move(dt, keysPressed)

    // clear the screen
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)
    gl.clearDepth(1.0f)
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)

    traceMesh.draw(this, camera, *quadrics, *lights)

  }
}
