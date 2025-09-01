package com.example.programmingailocal.data

// Sampling / decoding defaults tuned for a friendly, coherent on-device assistant
const val DEFAULT_MAX_TOKEN = 512      // shorter answers keep memory + latency low
const val DEFAULT_TOPK        = 32     // limit to 32 probable next tokens
const val DEFAULT_TOPP        = 0.95f  // nucleus sampling 95 %
const val DEFAULT_TEMPERATURE = 0.7f   // slightly creative but not chaotic

// Max number of images allowed when sending an image prompt
const val MAX_IMAGE_COUNT = 10 