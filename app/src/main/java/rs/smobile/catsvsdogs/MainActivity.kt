package rs.smobile.catsvsdogs

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import rs.smobile.catsvsdogs.data.local.model.Image
import rs.smobile.catsvsdogs.ui.theme.CatsVsDogsTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val image by viewModel.image.collectAsStateWithLifecycle()
            val expectedLabel by viewModel.expectedLabel.collectAsStateWithLifecycle()
            val classifiedLabel by viewModel.classifiedLabel.collectAsStateWithLifecycle()
            val generatedCaption by viewModel.generatedCaption.collectAsStateWithLifecycle()

            val context = LocalContext.current
            val imageLoader = context.imageLoader

            LaunchedEffect(Unit) {
                viewModel.loadRandomImage()
            }

            LaunchedEffect(image) {
                val request = ImageRequest.Builder(context)
                    .data(image?.url)
                    .allowHardware(false)
                    .target { drawable ->
                        (drawable as? BitmapDrawable)?.bitmap?.let {
                            viewModel.inferImageClass(it)
                        }
                    }
                    .build()
                imageLoader.enqueue(request)
            }

            CatsVsDogsTheme {
                MainScreen(
                    image = image,
                    expectedLabel = expectedLabel,
                    classifiedLabel = classifiedLabel,
                    generatedCaption = generatedCaption,
                    onShowImage = viewModel::loadRandomImage,
                )
            }
        }
    }
}

@Composable
private fun MainScreen(
    modifier: Modifier = Modifier,
    image: Image?,
    expectedLabel: String,
    classifiedLabel: String,
    generatedCaption: String,
    onShowImage: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(top = 24.dp)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.expected_label),
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = expectedLabel,
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.classified_label),
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = classifiedLabel.capitalize(),
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            ImageBox(
                image = image,
                onShowImage = onShowImage
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.caption_label),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(
                text = generatedCaption.capitalize(),
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ImageBox(
    modifier: Modifier = Modifier,
    image: Image?,
    onShowImage: () -> Unit,
) {
    Box(
        modifier = modifier
            .wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            AsyncImage(
                model = image?.url,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onShowImage),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(
                    image = ImageVector.vectorResource(R.drawable.ic_launcher_foreground)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    CatsVsDogsTheme {
        MainScreen(
            image = null,
            expectedLabel = "Cat",
            classifiedLabel = "dog",
            generatedCaption = "A cute cat is laying on her stomach",
        ) { }
    }
}