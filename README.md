# AI Edge Gallery

A comprehensive collection of on-device AI demonstrations and Android applications showcasing local Large Language Models (LLMs), multimodal inference capabilities, and privacy-preserving artificial intelligence solutions.

## Overview

AI Edge Gallery provides developers with ready-to-use examples and reference implementations for deploying AI models directly on Android devices. This approach ensures user privacy, reduces latency, and enables offline functionality by eliminating the need for cloud-based AI services.

### Key Benefits

**Privacy First**: All AI processing occurs locally on the device, ensuring sensitive data never leaves the user's control.

**Offline Capability**: Applications function without internet connectivity, making them reliable in any environment.

**Low Latency**: Direct on-device inference eliminates network delays for real-time AI interactions.

**Cost Effective**: No ongoing API costs or cloud service dependencies.

## Project Structure

### Core Components

**`gallery/Android`**
Reference Android application containing foundational examples and best practices for implementing on-device AI. This serves as the primary learning resource for developers new to edge AI development.

**`ProgrammingAiLocal/`**
Full-featured Android application demonstrating local LLM integration with practical use cases including code assistance, text generation, and conversational AI interfaces.

**`Programming__AiLocal/`**
Alternative implementation variant providing different architectural approaches and UI patterns for local AI integration.

**`mistral_model/`**
Comprehensive collection of model assets, configuration files, and experimental implementations focused on Mistral model variants optimized for mobile deployment.

**`ProgrammingAiLocal/qwen25-therapy-bot/`**
Lightweight Python demonstration showcasing therapeutic conversation capabilities using the Qwen 2.5 model, perfect for rapid prototyping and testing.

## Technical Features

### On-Device Inference Engine
- Optimized model loading and memory management
- Efficient tensor operations for mobile hardware
- Battery-conscious processing with configurable performance modes

### Local LLM Integration
- Support for multiple model formats (ONNX, TensorFlow Lite, Core ML)
- Dynamic model switching and configuration
- Streaming text generation with real-time response display

### User Interface Components
- Pre-built chat interfaces with customizable themes
- Voice input and text-to-speech integration
- Accessibility features and internationalization support

### Development Tools
- Gradle-based build system with dependency management
- Automated model downloading and validation
- Debug tools for performance monitoring and optimization

## System Requirements

### Android Development
- **Android Studio**: Giraffe (2022.3.1) or later recommended
- **Android SDK**: API level 21+ (Android 5.0) minimum
- **Android NDK**: Required for native model inference libraries
- **JDK**: Version 17 (bundled with Android Studio)
- **Device RAM**: Minimum 4GB recommended for standard models, 8GB+ for larger models

### Python Development (Optional)
- **Python**: Version 3.10 or higher
- **pip**: Latest version for dependency management
- **Virtual Environment**: Recommended for isolation

### Hardware Considerations
- **CPU**: ARM64 architecture preferred for optimal performance
- **Storage**: 2-10GB free space depending on selected models
- **RAM**: Varies by model size (see model specifications)

## Installation and Setup

### Android Project Setup

#### Method 1: Android Studio GUI
1. Launch Android Studio and select "Open an Existing Project"
2. Navigate to your desired project directory:
   - `ProgrammingAiLocal/` for the primary application
   - `Programming__AiLocal/` for the alternative variant  
   - `gallery/Android/src/app` for reference samples
3. Allow Gradle to sync dependencies (this may take several minutes)
4. Connect an Android device via USB or start an emulator
5. Click "Run" or press Shift+F10 to build and deploy

#### Method 2: Command Line Build
```bash
# Clone the repository
git clone [repository-url]
cd ai-edge-gallery

# Build primary application
./gradlew :ProgrammingAiLocal:app:assembleDebug

# Build alternative variant
./gradlew :Programming__AiLocal:app:assembleDebug

# Install to connected device
./gradlew :ProgrammingAiLocal:app:installDebug
```

### Python Demo Setup
```bash
# Navigate to Python demo directory
cd ProgrammingAiLocal/qwen25-therapy-bot/

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run the demo
python therapy_bot.py
```

## Model Configuration

### Supported Model Formats
- **ONNX Runtime**: Cross-platform inference with CPU and GPU acceleration
- **TensorFlow Lite**: Optimized for mobile and edge devices
- **PyTorch Mobile**: Native PyTorch model deployment
- **Core ML**: Apple ecosystem optimization (when applicable)

### Model Management

#### Automatic Download
The applications include built-in model downloaders that fetch optimized models from verified sources. Models are validated using checksums defined in `model_allowlist.json`.

#### Manual Installation
For custom models or offline scenarios:

1. Place model files in the designated directory:
   - Android: `app/src/main/assets/models/`
   - Or use the configurable models directory in app settings

2. Update model configuration:
   ```json
   {
     "model_name": "custom_model",
     "file_path": "models/custom_model.onnx",
     "input_length": 2048,
     "quantization": "int8"
   }
   ```

3. Restart the application to detect new models

### Model Allowlist Configuration
The `model_allowlist.json` file defines approved models with their specifications:

```json
{
  "models": [
    {
      "name": "qwen2.5-0.5b-instruct",
      "size": "500MB",
      "description": "Lightweight conversational model",
      "url": "https://example.com/model.onnx",
      "checksum": "sha256:abc123..."
    }
  ]
}
```

## Architecture Overview

### Android Application Architecture

**Presentation Layer**
- Activities and Fragments for user interaction
- View Models for UI state management
- Data binding for reactive UI updates

**Business Logic Layer**
- AI inference services and model management
- Conversation history and context handling
- Background processing and threading

**Data Layer**
- Model file storage and caching
- User preferences and settings
- Conversation persistence (optional)

### Inference Pipeline

1. **Model Loading**: Efficient loading with memory mapping
2. **Input Processing**: Tokenization and tensor preparation
3. **Inference Execution**: Optimized computation on available hardware
4. **Output Processing**: Decoding and post-processing of results
5. **Response Streaming**: Real-time delivery of generated content

## Development Workflows

### Building and Testing

#### Debug Builds
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

#### Release Builds
```bash
# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Generate signed AAB for Play Store
./gradlew bundleRelease
```

### Performance Optimization

#### Memory Management
- Monitor heap usage with Android Studio profiler
- Implement model caching strategies
- Use memory mapping for large model files

#### Battery Optimization
- Configure inference frequency based on usage patterns
- Implement background processing limits
- Provide user controls for performance vs. battery trade-offs

## Troubleshooting Guide

### Common Build Issues

**Gradle Sync Failures**
```bash
# Clean and rebuild project
./gradlew clean
./gradlew build --refresh-dependencies
```

**SDK Path Issues**
Ensure `local.properties` contains correct paths:
```properties
sdk.dir=/path/to/Android/Sdk
ndk.dir=/path/to/Android/Sdk/ndk/[version]
```

### Runtime Issues

**Out of Memory Errors**
- Use smaller model variants
- Reduce input context length
- Close other applications to free RAM
- Enable model quantization in settings

**Model Loading Failures**
- Verify model file integrity
- Check available storage space
- Ensure model format compatibility
- Review application logs for detailed error messages

**Performance Issues**
- Test on different devices to identify hardware limitations
- Monitor CPU and GPU usage
- Adjust inference parameters (batch size, precision)
- Consider model optimization techniques

### Debug Tools

**Logging Configuration**
Enable detailed logging in `build.gradle`:
```gradle
buildTypes {
    debug {
        buildConfigField "boolean", "ENABLE_LOGGING", "true"
    }
}
```

**Performance Monitoring**
Use Android Studio profiler to analyze:
- Memory allocation patterns
- CPU usage during inference
- GPU utilization (if applicable)
- Network activity (for model downloads)

## Contributing Guidelines

### Code Standards
- Follow Android development best practices
- Use consistent naming conventions
- Include comprehensive documentation
- Write unit tests for critical functionality

### Pull Request Process
1. Fork the repository and create a feature branch
2. Implement changes with appropriate tests
3. Update documentation as needed
4. Submit pull request with detailed description
5. Address code review feedback promptly

### Issue Reporting
When reporting issues, include:
- Device specifications and Android version
- Detailed steps to reproduce
- Relevant log outputs
- Expected vs. actual behavior

## Licensing and Legal

This project is licensed under the Apache License 2.0, which permits use, modification, and distribution of the code with attribution. See the `LICENSE` file for complete terms.

### Model Licensing
Individual AI models may have their own licensing terms. Users are responsible for compliance with model-specific licenses when using or redistributing models.

### Privacy Considerations
While on-device processing enhances privacy, developers should still implement appropriate data handling practices and provide clear privacy policies to users.

## Community and Support

### Resources
- **Documentation**: Comprehensive guides in the `/docs` directory
- **Examples**: Sample implementations in `/gallery` and project directories
- **Issue Tracker**: Report bugs and request features via GitHub Issues
- **Discussions**: Community support and collaboration via GitHub Discussions

### Getting Help
For technical support:
1. Check the troubleshooting guide above
2. Search existing GitHub Issues
3. Review sample implementations
4. Post detailed questions in GitHub Discussions

This documentation provides a foundation for getting started with AI Edge Gallery. As the project evolves, additional features and improvements will be documented in release notes and updated guides.
