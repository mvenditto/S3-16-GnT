package main_system.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Rectangle
import com.unibo.s3.main_system.util.GntUtils
import main_system.GdxDependencies
import org.junit.runner.RunWith
import org.junit.{After, Before, Test}

@RunWith(classOf[GdxDependencies])
class GntUtilsTest {

  private[this] var testMapFile: FileHandle = _
  private[this] val EmptyString = ""

  @Before def init(): Unit = {
    testMapFile = Gdx.files.local("test_map.txt")
    testMapFile.writeString(EmptyString, false)
  }

  @Test def testCorrectParseAsRectangles(): Unit = {
    val testData = "3.0:9.0:2:2\n3.0:17.0:2:2"
    val expected = List(new Rectangle(3f,9f,2f,2f),new Rectangle(3f,17f,2f,2f))
    testMapFile.writeString(testData, false)
    assert(GntUtils.parseMapToRectangles(testMapFile).equals(expected))
  }

  @After def cleanup(): Unit = testMapFile.delete()

}
