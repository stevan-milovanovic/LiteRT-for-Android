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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
            val generatedDescription by viewModel.generatedDescription.collectAsStateWithLifecycle()
            val generatedDescriptionIsLoading by viewModel.generatedDescriptionIsLoading.collectAsStateWithLifecycle()

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
                            viewModel.generateCaptionForImage(it)
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
                    llmName = viewModel.getLlmName(),
                    generatedDescription = generatedDescription,
                    generatedDescriptionIsLoading = generatedDescriptionIsLoading,
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
    llmName: String,
    generatedDescription: String,
    generatedDescriptionIsLoading: Boolean,
    onShowImage: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(top = 16.dp)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.expected_label),
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = expectedLabel,
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
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
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = classifiedLabel.capitalize(),
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            ImageBox(
                image = image,
                onShowImage = onShowImage
            )
            Text(
                text = stringResource(R.string.caption_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = generatedCaption.capitalize(),
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.gemma_3_1b_it_cpu_generated_description, llmName),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Card(
                modifier = Modifier.padding(vertical = 4.dp),
                shape = RoundedCornerShape(4.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        text = generatedDescription,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (generatedDescriptionIsLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
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
            llmName = "Gemma 3",
            generatedDescription = "This is generated description",
            generatedDescriptionIsLoading = true,
        ) { }
    }
}