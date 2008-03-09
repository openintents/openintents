package org.openintents.samples.openglsensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
/**
 * Displays a pyramid.
 * 
 * The pyramid is taken from an anddev.org tutorial.
 *
 */

public class Pyramid { 
      
     private IntBuffer mVertexBuffer; 
     private IntBuffer mColorBuffer; 
     private ByteBuffer mIndexBuffer; 

     public Pyramid() { 
                     
          int one = 0x10000; 
          /* Every vertex got 3 values, for 
          * x / y / z position in the kartesian space. 
          */ 
          int vertices[] = { -one, -one, -one, // The four floor vertices of the pyramid 
               one, -one, -one, 
               one, one, -one, 
               -one, one, -one, 
               0, 0, one, };  // The top of the pyramid 

          /* Every vertex has got its own color, described by 4 values 
          * R(ed) 
          * G(green) 
          * B(blue) 
          * A(lpha) <-- Opacity 
          */ 
          int colors[] = { 0, 0, one, one, 
               one, 0, 0, one, 
               one, one, 0, one, 
               0, one, 0, one, 
               one, 0, one, one, }; 

           /* The last thing is that we need to describe some Triangles. 
           * A triangle got 3 vertices. 
           * It is important in which order 
           * the vertices of each triangle are described. 
           * So describing a triangle through the vertices: "0, 3, 4" 
           * will not result in the same triangle as: "0, 4, 3" 
           * The reason for that is the call of: "gl.glFrontFace(gl.GL_CW);" 
           * which means, that we have to describe the "visible" side of the 
           * triangles by naming its vertices in a ClockWise order! 
           * From the other side, the triangle will be 100% transparent! 
           */ 
          byte indices[] = { 0, 4, 1, // The four side-triangles 
               1, 4, 2, 
               2, 4, 3, 
               3, 4, 0, 
               1, 2, 0, // The two bottom-triangles 
               0, 2, 3}; 

          // Buffers to be passed to gl*Pointer() functions 
          // must be direct, i.e., they must be placed on the 
          // native heap where the garbage collector cannot 
          // move them. 
          // 
          // Buffers with multi-byte datatypes (e.g., short, int, float) 
          // must have their byte order set to native order 

          ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);  // * 4 becuase of int 
          vbb.order(ByteOrder.nativeOrder()); 
          mVertexBuffer = vbb.asIntBuffer(); 
          mVertexBuffer.put(vertices); 
          mVertexBuffer.position(0); 

          ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4); // * 4 becuase of int 
          cbb.order(ByteOrder.nativeOrder()); 
          mColorBuffer = cbb.asIntBuffer(); 
          mColorBuffer.put(colors); 
          mColorBuffer.position(0); 

          mIndexBuffer = ByteBuffer.allocateDirect(indices.length); 
          mIndexBuffer.put(indices); 
          mIndexBuffer.position(0); 
     } 

     public void draw(GL10 gl) { 
          gl.glFrontFace(gl.GL_CW); 
          gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer); 
          gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer); 
          gl.glDrawElements(gl.GL_TRIANGLES, 18, gl.GL_UNSIGNED_BYTE, mIndexBuffer); 
     } 

}
