package com.example.mykidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context : Context, attrs: AttributeSet) : View(context, attrs) {

    //most classes from android.graphics
    private var mDrawPath: CustomPath? = null //Our own custom Path type class (inherits Path class)
    private var mCanvasBitmap: Bitmap? = null //To store Bitmap
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK //store color
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>() //store drawn path
    private val mUndoPaths = ArrayList<CustomPath>()
    /**
     * Initialize and set variables
     */
    init {
        setUpDrawing() //call function to set variables
    }

    /**
     * Function that takes last entry in mPaths out and runs onDraw
     * resulting in taking last stroke out of canvas
     */
    fun onClickUndo (){
        if (mPaths.size > 0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))

            //calls onDraw somehow
            invalidate()
        }
    }
    /**
     * Function used in inti to set varibles
     */
    private fun setUpDrawing (){
        mDrawPaint = Paint() //using paint class and default settings
        mDrawPath = CustomPath(color,mBrushSize)  //setup with custom class
        mDrawPaint!!.color = color //change from default to our settings
        mDrawPaint!!.style = Paint.Style.STROKE //change from default to our settings
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND //change from default to our settings
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND //change from default to our settings
        mCanvasPaint = Paint(Paint.DITHER_FLAG) /** Not sure */
        //mBrushSize = 20.toFloat() //set brush size (dont need as we set it with function)
    }
    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * we over ride this so We set bitmap to current screen height and width
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //create Bitmap using Bitmap class with screen size
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        //set canvas using bitmap just created
        canvas = Canvas(mCanvasBitmap!!)
    }
    /**
     * Not sure when its called but part of the view class
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f,0f, mCanvasPaint)

        //mPaths has previous lines drawn and this loops through keeping them on bitmap.
        //Not sure when this is called
        for (path in mPaths) {
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }


        if(!mDrawPath!!.isEmpty) {//Null check

            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color

            canvas.drawPath(mDrawPath!!, mDrawPaint!!) //We can do this because our custom class inherits path class
        }
    }

    /**
     *When user touches screen this is called.
     * Part of the view class which we inherite into this class so we can overide function
     */

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        //Store x and y of finger action
        val touchX = event?.x
        val touchY = event?.y

        //Switch to the action that was triggered
        when(event?.action){

            //pushed on screen
            MotionEvent.ACTION_DOWN -> {

                //
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                //not sure
                mDrawPath!!.reset()

                if (touchX != null) {//null check
                    if (touchY != null) {//null check
                        mDrawPath!!.moveTo(touchX, touchY)
                    }
                }
            }

            //User is drawing on screen
            MotionEvent.ACTION_MOVE -> {

                if (touchX != null) {//null check
                    if (touchY != null) {//null check

                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color,mBrushSize)
            }
            else -> return false
        }

        //calls onDraw somehow
        invalidate()

        return true
    }

    fun setSizeForBrush (newSize: Float){

        //This takes into account of the devices screen size
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, resources.displayMetrics)

        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor (newColor : String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }


    /**
     * Custom class for mDrawpath inherits Class of Path
     */
    internal inner class CustomPath(var color : Int, var brushThickness: Float) : Path() {

    }


}