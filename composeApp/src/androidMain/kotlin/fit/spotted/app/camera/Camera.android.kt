package fit.spotted.app.camera

actual fun getCamera(): Camera = CommonCameraK(AndroidCameraKPlatform())
