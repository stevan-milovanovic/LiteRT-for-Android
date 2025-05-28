package rs.smobile.catsvsdogs

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = onShowImage) {
                        Icon(
                            painter = painterResource(R.drawable.ic_generative_ai),
                            contentDescription = "Generate image description"
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(innerPadding)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            ImageClassificationComponent(
                expectedLabel = expectedLabel,
                classifiedLabel = classifiedLabel
            )
            ImageBox(image = image)
            ImageCaptionerComponent(
                generatedCaption = generatedCaption
            )
            LlmComponent(
                llmName = llmName,
                generatedDescription = generatedDescription,
                generatedDescriptionIsLoading = generatedDescriptionIsLoading,
            )
        }
    }
}

@Composable
private fun ImageClassificationComponent(
    expectedLabel: String,
    classifiedLabel: String,
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
            text = expectedLabel.capitalize(),
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
}

@Composable
private fun ImageCaptionerComponent(
    generatedCaption: String,
) {
    Text(
        text = stringResource(R.string.caption_label),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = generatedCaption.capitalize(),
        modifier = Modifier.padding(vertical = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun LlmComponent(
    llmName: String,
    generatedDescription: String,
    generatedDescriptionIsLoading: Boolean,
) {
    Text(
        text = stringResource(R.string.gemma_3_1b_it_cpu_generated_description, llmName),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Card(
        modifier = Modifier.padding(vertical = 16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(
            Modifier
                .wrapContentSize()
                .padding(12.dp)
        ) {
            TextWithLoadingAnimation(
                text = generatedDescription,
                isLoading = generatedDescriptionIsLoading
            )
        }
    }
}

@Composable
private fun TextWithLoadingAnimation(
    text: String,
    intervalMs: Long = 350L,
    maxDots: Int = 3,
    isLoading: Boolean = true
) {
    var dotCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMs)
            dotCount = (dotCount + 1) % (maxDots + 1)
        }
    }
    val style = MaterialTheme.typography.bodyMedium
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (text.isEmpty()) stringResource(R.string.loading) else text,
            style = style
        )
        if (isLoading || text.isEmpty()) {
            Text(
                text = ".".repeat(dotCount),
                style = style
            )
        }
    }
}

@Composable
private fun ImageBox(
    modifier: Modifier = Modifier,
    image: Image?,
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            AsyncImage(
                model = image?.url,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.fillMaxSize(),
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