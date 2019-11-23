package com.seanghay.khmervehiclescanner

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import org.jsoup.Jsoup
import java.util.concurrent.Executors


class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val executer = Executors.newSingleThreadExecutor()
    private val analyzer = QRcodeAnalyzer()
    private val compositeDisposable = CompositeDisposable()
    private val adapter = VehicleAdapter(emptyList())
    private var isCameraStarted = false
    private var preview: Preview? = null

    private val pattern = Regex("http[s]?://ts.mpwt.gov.kh/\\w+")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false).apply {
            this.sheet.visibility = View.INVISIBLE
            this.scrim.visibility = View.INVISIBLE

            this.recyclerView?.let {
                it.adapter = adapter
                it.setHasFixedSize(true)
            }

            doOnApplyWindowInsets { view, insets, padding ->
                sheet.recyclerView.updatePadding(
                    bottom = sheet.recyclerView.paddingBottom + insets.systemWindowInsetBottom
                )
                nav.updatePadding(bottom = insets.systemWindowInsetBottom)
            }

            viewModel.isLoading.observe(viewLifecycleOwner, Observer {
                progressBar.isGone = !it
            })

            fabScan.isEnabled = false
            fabScan.visibility = View.INVISIBLE
            fabScan.scaleX = 0f
            fabScan.scaleY = 0f
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initNav()

        viewModel.showSheet.observe(viewLifecycleOwner, Observer {
            showSheet(it)
        })

        scrim.setOnClickListener {
            viewModel.showSheet.value = false
        }


        fabScan.post {
            animateFab(fabScan)
        }

        fabScan.setOnClickListener {
            if (isCameraStarted) {
                preview?.enableTorch(!(preview?.isTorchOn ?: false))
            } else {
                requestCamera()
            }
        }

        viewModel.scanStarted.observe(viewLifecycleOwner, Observer {
            animate(!it)
            textureViewCamera?.isVisible = it
        })

        textureViewCamera?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransforms()
        }

        analyzer.getResultFlowable()
            .observeOn(AndroidSchedulers.mainThread())
            .filter(::validateQrCode)
            .subscribe {
                if (it.isNotEmpty())
                    if (viewModel.isLoading.value == false && viewModel.showSheet.value == false) {
                        viewModel.isLoading.value = true
                        request(it)
                    }

            }.addTo(compositeDisposable)

    }

    private fun initNav() {
        nav.setOnNavigationItemSelectedListener {
            if (it.itemId == R.id.settings) {
                Toast.makeText(requireContext(), "Available sooooon! xD", Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    private fun animateFab(
        it: ExtendedFloatingActionButton,
        delay: Long = 0L,
        done: () -> Unit = {}
    ) {

        val interpolator = AccelerateDecelerateInterpolator()

        if (it.isEnabled) {
            ViewCompat.animate(it)
                .scaleX(0f)
                .scaleY(0f)
                .setStartDelay(delay)
                .setInterpolator(interpolator)
                .withStartAction {
                    it.isEnabled = false
                }
                .withEndAction {
                    it.visibility = View.INVISIBLE
                    it.isEnabled = false
                }
                .setDuration(250L)
                .withEndAction(done)
                .setUpdateListener { v ->
                    nav.curveRadius = (v.height / 2f) * v.scaleX
                }
                .start()
        } else {
            it.visibility = View.VISIBLE
            it.isEnabled = true

            ViewCompat.animate(it)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(delay)
                .withEndAction(done)
                .setInterpolator(interpolator)
                .setDuration(250L)
                .setUpdateListener { v ->
                    nav.curveRadius = (v.height / 2f) * v.scaleX
                }
                .start()
        }
    }

    private fun request(url: String) {
        HttpService.get().getInfo(url)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                viewModel.isLoading.value = false
            }
            .subscribeBy(onError = {
                it.printStackTrace()
            }) {
                parseHtml(it.string())
            }.addTo(compositeDisposable)
    }

    private fun parseHtml(html: String) {
        val doc = Jsoup.parse(html)
        val english = doc.select("#english")
        val rows = english.select("table tbody tr")

        val values = rows.map {
            val content = it.select("td")
            val type = content.first().text()
            val value = content[1].text()

            VehicleItem(type, value)
        }

        adapter.updateItems(values)
        recyclerView?.post {
            recyclerView.scrollToPosition(0)
        }
        viewModel.showSheet.value = true
        viewModel.isLoading.value = false
    }

    private fun showSheet(showSheet: Boolean) {
        if (showSheet) {
            if (sheet.visibility == View.VISIBLE) return
            sheet.translationY = sheet.height.toFloat()
            sheet.visibility = View.VISIBLE
            scrim.visibility = View.VISIBLE

            sheet.animate().translationY(0f)
                .setDuration(250)
                .setUpdateListener {
                    scrim.alpha = it.animatedFraction
                }
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

        } else {
            if (sheet.visibility == View.GONE) return

            scrim.visibility = View.VISIBLE
            sheet.visibility = View.VISIBLE

            sheet.animate().translationY(sheet.height.toFloat())
                .setDuration(200)
                .setUpdateListener {
                    scrim.alpha = 1f - it.animatedFraction
                }
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    sheet.visibility = View.GONE
                    scrim.visibility = View.GONE
                }
                .start()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()

    }

    private fun validateQrCode(text: String): Boolean {
        return pattern.matches(text)
    }


    private fun updateTransforms() {
        val matrix = Matrix()
        val centerX = textureViewCamera.width / 2f
        val centerY = textureViewCamera.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (textureViewCamera.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        // matrix.postScale(1f + relative, 1f, centerX, centerY)
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        textureViewCamera.setTransform(matrix)
    }



    private fun startCamera() {
        // Set secure flags so it won't show in recent apps
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        animateFab(fabScan) {
            fabScan.setIconResource(R.drawable.ic_flash)
            animateFab(fabScan, 500)
        }


        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            setLensFacing(CameraX.LensFacing.BACK)
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = textureViewCamera.parent as ViewGroup
            parent.removeView(textureViewCamera)
            parent.addView(textureViewCamera, 0)
            textureViewCamera.surfaceTexture = it.surfaceTexture
            updateTransforms()
        }

        this.preview = preview


        val analysisConfig = ImageAnalysisConfig.Builder().apply {
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analysis = ImageAnalysis(analysisConfig)
        analysis.setAnalyzer(executer, analyzer)

        CameraX.bindToLifecycle(this, preview, analysis)

        viewModel.scanStarted.value = true
        isCameraStarted = true
    }


    private fun animate(show: Boolean) {
        val infoTargetAlpha = if (show) 0f else 1f

        val targetAlpha = if (show) 1f else 0f
        val targetY = if (show) 0f else -imageViewPlate.height.toFloat() / 2f

        val alphaHolder = PropertyValuesHolder.ofFloat(View.ALPHA, targetAlpha)
        val infoAlphaHolder = PropertyValuesHolder.ofFloat(View.ALPHA, infoTargetAlpha)
        val translationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, targetY)


        if (show) {
            focusView.alpha = 0f
        }

        val focusAnimator =
            ObjectAnimator.ofPropertyValuesHolder(focusView, infoAlphaHolder).apply {
                duration = 300
            }

        val infos = arrayOf<View>(scanQr, scanQrDesc).map {
            it.visibility = View.VISIBLE

            if (show) {
                it.alpha = 0f
            }

            ObjectAnimator.ofPropertyValuesHolder(it, infoAlphaHolder).apply {
                duration = 200
            }

        }

        val plateAnimator =
            ObjectAnimator.ofPropertyValuesHolder(
                imageViewPlate,
                alphaHolder,
                translationY
            ).apply {
                duration = 200
            }

        val titleAnimator = ObjectAnimator.ofPropertyValuesHolder(
            textViewTitle,
            alphaHolder,
            translationY
        ).apply {
            duration = 200
        }

        val descAnimator =
            ObjectAnimator.ofPropertyValuesHolder(
                textViewDesc,
                alphaHolder,
                translationY
            ).apply {
                duration = 200
            }

        val animatorSet = AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            play(plateAnimator).before(titleAnimator)
            play(titleAnimator).before(descAnimator)
            play(descAnimator).after(titleAnimator)
            infos.forEach { play(it).after(descAnimator) }
            play(focusAnimator).after(infos.last())

        }

        animatorSet.start()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) requestCamera()

    }


    private fun requestCamera() {
        if (isCameraGranted()) {
            startCamera()
        } else {
            if (isCameraRationale()) {
                showRationaleDialog()
            } else requestCameraPermission()
        }
    }

    private fun showRationaleDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.permission_required)
            .setMessage(R.string.permission_required_message)
            .setPositiveButton(R.string.enable) { dialog, which ->
                openPermissionSettings()
            }
            .setNegativeButton(R.string.close) { dialog, which ->

            }.create()

        dialog.show()
    }

    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        startActivityForResult(intent, 2)
    }


    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
    }

    private fun isCameraRationale(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }

    private fun isCameraGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    setOnApplyWindowInsetsListener { v, insets ->
        f(v, insets, initialPadding)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

data class InitialPadding(
    val left: Int, val top: Int,
    val right: Int, val bottom: Int
)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)
