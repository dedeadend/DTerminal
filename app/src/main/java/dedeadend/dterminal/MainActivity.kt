package dedeadend.dterminal

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dedeadend.dterminal.ui.main.Main
import dedeadend.dterminal.ui.theme.DTerminalTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setOnExitAnimationListener { splashScreenView ->
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 3f)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 3f)
                val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)

                ObjectAnimator.ofPropertyValuesHolder(
                    splashScreenView.iconView,
                    scaleX, scaleY, alpha
                ).apply {
                    interpolator = AnticipateInterpolator()
                    duration = 500L
                    doOnEnd {
                        splashScreenView.remove()
                    }
                    start()
                }
            }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DTerminalTheme {
                Main()
            }
        }
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    DTerminalTheme {
//        Main()
//    }
//}