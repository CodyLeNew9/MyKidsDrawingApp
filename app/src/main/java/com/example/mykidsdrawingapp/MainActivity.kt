package com.example.mykidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var mImageButtonCurrentPaint : ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setSizeForBrush(20.toFloat())

        //set current paint to black at start (because thats what be have choosen in DrawingView class)
        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton

        //Mark the selected color have different src
        mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))


        //Brush Button
        val brushBtn = ib_brush
        brushBtn.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        //Gallery Button
        val galleryBtn = ib_gallery
        galleryBtn.setOnClickListener {
            //check for permission
            if (isReadStorageAllowed()) {
                //new intent and store in varible.
                val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //start intent
                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission() //we need permission
            }
        }

        //Undo button
        val undoBtn = ib_undo
        undoBtn.setOnClickListener {
            drawing_view.onClickUndo()
        }

        //Save Button
        val saveBtn = ib_save
        saveBtn.setOnClickListener {

            //check for permission
            if (isReadStorageAllowed()) {

                val bitmap = getBitmapFromView(fl_drawing_view_container)
                BitmapAsyncTask(bitmap).execute()

            } else {

                requestStoragePermission() //we need permission

            }
        }
    }


    /**
     * override result so we can check for success and add image to background
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) { //was the result ok
            if (requestCode == GALLERY) {//check if activity was for gallery
                try {
                    if(data!!.data != null) { //null check
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    } else { //null
                        Toast.makeText(this,"ERROR in parsing the image",Toast.LENGTH_SHORT).show()
                    }
                } catch (e : Exception) { //generic error
                    e.printStackTrace() //prints to log
                }
            }
        }
    }


    /**
     * Creating our own dialog for the popup of selecting brushes
     */
    private fun showBrushSizeChooserDialog(){

        //creating dialog into variable using this activity as context
        val brushDialog = Dialog(this)

        //set the view to our created xml
        brushDialog.setContentView(R.layout.dialog_brush_size)
        //set title
        brushDialog.setTitle("Brush Size: ")

        //getting id from dialog/xml file into variable
        val smallBtn = brushDialog.ib_small_brush
        val mediumBtn = brushDialog.ib_medium_brush
        val largeBtn = brushDialog.ib_large_brush

        //setting on click listener to variable
        smallBtn.setOnClickListener {
            drawing_view.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        }

        mediumBtn.setOnClickListener {
            drawing_view.setSizeForBrush(15.toFloat())
            brushDialog.dismiss()
        }

        largeBtn.setOnClickListener {
            drawing_view.setSizeForBrush(25.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }


    /**
     * function called from xml when a color is picked
     * we take the tag from in and also change the src
     */
    fun paintClicked (view:View) {

        if (view !== mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString() //color tag reads #000000 color set in resorce file

            drawing_view.setColor(colorTag)

            //Mark selected color have different src
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

            //Mark selected color have different src
            mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))

            //set current paint to variable for comparison next selection
            mImageButtonCurrentPaint = imageButton

        }
    }

    /**
     * function called if isReadStorageAllowed comes back false and we need permission
     */
    private fun requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale (this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).toString() )) {
            Toast.makeText(this,"Need permission to add a background",Toast.LENGTH_SHORT).show()
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    /**
     * not need but overide results so we get visiual of accepting permission
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * function called to check if we have permission first
     */
    private fun isReadStorageAllowed () : Boolean {

        //store result in varible
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        //compare ints to return boolean
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView (view: View) : Bitmap {
        //create bitmap
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        // create canvas from bitmap
        val canvas = Canvas(returnedBitmap)

        //get bg
        val bgDrawable = view.background

        //check bg
        if (bgDrawable != null) {//null check
            bgDrawable.draw(canvas)//draw bg on canvas
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas) //draw view on canvas

        //This doesnt make sense now we return bitmap? what about canvas
        //I dont understand how we changed this varible but aparenly it does
        return returnedBitmap
    }

    private inner class BitmapAsyncTask(val mBitmap: Bitmap) : AsyncTask <Any, Void, String>() {

        private lateinit var mProgressDialog : Dialog

        override fun onPreExecute() {
            super.onPreExecute()

            showProgressDialog()
        }


        override fun doInBackground(vararg params: Any?): String {
            var result = ""

            if (mBitmap != null){//null check
                try {
                    val bytes = ByteArrayOutputStream()

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(externalCacheDir!!.absoluteFile.toString() + File.separator+ "KidDrawingApp_" + System.currentTimeMillis()/1000 + ".png")

                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()

                    result = f.absolutePath

                } catch (e : Exception) { //general exception
                    result = ""
                }
            }

            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            cancelProgressDialog()

            if (result!!.isNotEmpty()) {
                Toast.makeText(this@MainActivity, "File Saved Successfully: $result", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Something went wrong when saving the file", Toast.LENGTH_SHORT).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null) {
                path, uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"

                startActivity(Intent.createChooser(shareIntent, "Share"))
            }
        }

        private fun showProgressDialog (){
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog.show()
        }

        private fun cancelProgressDialog (){
            mProgressDialog.dismiss()
        }
    }

    //object for constants
    companion object {
        private const val STORAGE_PERMISSION_CODE =1
        private const val GALLERY = 2
    }
}