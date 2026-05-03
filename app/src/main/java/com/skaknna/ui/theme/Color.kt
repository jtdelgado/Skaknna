package com.skaknna.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// MODERN CLASSIC DESIGN SYSTEM - Color Palette
// ============================================================================

// Utility Colors
val BlackColor = Color(0xFF000000)
val WhiteColor = Color(0xFFFFFFFF)
val TransparentColor = Color(0x00000000)

// ============================================================================
// PRIMARY SEMANTIC COLORS (Dark Theme)
// ============================================================================

// Primary: Forest Green for main interactions and surfaces
val PrimaryGreen = Color(0xFF1B5E20)

// Secondary: Soft Amber for accents, CTAs, and highlights
val PrimaryGold = Color(0xFFE6C85C)

// Tertiary: Leaf Green for positive/success states
val LeafGreen = Color(0xFF4CAF50)

// Surface: Charcoal with green touch for cards/dialogs/containers
val SurfaceGreen = Color(0xFF162B18) // Deeper green for modern depth
val SurfaceDark = Color(0xFF0C100D) // Ultra dark graphite green

// Text/Content: Warm off-white for contrast on dark surfaces
val WarmWhite = Color(0xFFFFF8E7)

// Deep Espresso: High contrast for text, borders, secondary content
val DeepEspresso = Color(0xFF5D4037)

// Outline: Subtle borders and dividers (1.dp replacement for shadows)
val OutlineColor = Color(0xFF556B2F) // Soft olive green

// ============================================================================
// BACKGROUND & GRADIENT COMPONENTS
// ============================================================================

// Background Gradient: Radial gradient center (almost black with green touch)
val BackgroundGradientCenter = Color(0xFF0C100D)

// Background Gradient: Radial gradient edges (forest green)
val BackgroundGradientEdge = Color(0xFF121B14)

// ============================================================================
// BOARD COMPONENTS
// ============================================================================

// Chess Board Squares: Matte cream (light squares)
val BoardCream = Color(0xFFE8DCC8)

// Chess Board Squares: Matte brown (dark squares)
val BoardBrown = Color(0xFF6B4F47)

// ============================================================================
// SEMANTIC COLORS (for accessibility & future light theme)
// ============================================================================

// Error: Red-pink for errors, destructive actions
val ErrorColor = Color(0xFFCF6679)

// Success: Green for successful operations
val SuccessColor = Color(0xFF4CAF50)

// Warning: Orange for warnings and cautions
val WarningColor = Color(0xFFFFB74D)

// Info: Amber for informational messages
val InfoColor = Color(0xFFE6C85C)

// ============================================================================
// TRANSPARENT OVERLAYS & GLASS EFFECTS
// ============================================================================

// Surface Glass: SurfaceDark with 90% opacity for overlay effects
val SurfaceGlass = Color(0xE61A1A1A)

// Gold Glow: Soft amber highlight for focus/hover states
val GoldGlow = Color(0x4DE6C85C) // 30% opacity

// Green Glow: Leaf green highlight for analysis/positive states
val GreenGlow = Color(0x334CAF50) // 20% opacity

// ============================================================================
// EVALUATION BAR COLORS (Stockfish Analysis)
// ============================================================================

// White Advantage: Clean white for positive evaluation
val EvaluationWhite = Color(0xFFF8F9FA) // Pure Bone White

// Black Advantage: Clean black for negative evaluation
val EvaluationBlack = Color(0xFF121212) // Pure Carbon