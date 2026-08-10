package com.monstera.harbor.feature.advanced

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.monstera.harbor.core.data.AppIconProvider
import com.monstera.harbor.core.topology.PackageName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HarborAppIcon(
    provider: AppIconProvider,
    packageName: PackageName,
    modifier: Modifier = Modifier.size(48.dp),
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val icon by produceState<Drawable?>(initialValue = null, provider, packageName) {
        value = withContext(Dispatchers.IO) { provider.load(packageName) }
    }
    AndroidView(
        modifier = modifier,
        factory = { ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_INSIDE } },
        update = { imageView ->
            imageView.contentDescription = contentDescription
            imageView.setImageDrawable(icon)
        },
    )
}
