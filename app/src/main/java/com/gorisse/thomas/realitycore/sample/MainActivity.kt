package com.gorisse.thomas.realitycore.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gorisse.thomas.realitycore.ARView
import com.gorisse.thomas.realitycore.entity.Model
import com.gorisse.thomas.realitycore.entity.ModelEntity

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
class MainActivity : AppCompatActivity() {

    private var backgroundARView: ARView? = null
    private var transparentARView: ARView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        backgroundARView = findViewById<ARView>(R.id.backgroundARView)
        transparentARView = findViewById<ARView>(R.id.transparentARView)
        //        transparentSceneView.setTransparent(true);
        loadModels()
    }

    fun loadModels() {
        val modelEntity = Model.load
        modelEntity.tr
//        ModelRenderable.builder()
//                .setSource(this
//                        , Uri.parse("models/model.glb"))
//                .setIsFilamentGltf(true)
//                .build()
//                .thenAccept(renderable -> {
//                    Node node1 = new Node();
//                    node1.setRenderable(renderable);
//                    node1.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
//                    node1.setLocalRotation(Quaternion.multiply(
//                            Quaternion.axisAngle(new Vector3(1f, 0f, 0f), 45),
//                            Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 75)));
//                    node1.setLocalPosition(new Vector3(0f, 0f, -1.0f));
//                    backgroundSceneView.getScene().addChild(node1);
//
//                    Node node2 = new Node();
//                    node2.setRenderable(renderable);
//                    node2.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
//                    node2.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 135));
//                    node2.setLocalPosition(new Vector3(0f, 0f, -1.0f));
//                    transparentSceneView.getScene().addChild(node2);
//                })
//                .exceptionally(
//                        throwable -> {
//                            Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG).show();
//                            return null;
//                        });
    }
}