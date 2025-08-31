# Academia App - Design System

## 🎨 Design Philosophy

Modern, accessible, and consistent design with **glass morphism**, **gradient accents**, and **smooth animations**. Built for educational excellence.

## 🎯 Core Principles

- **Glass Morphism**: Semi-transparent backgrounds with backdrop blur
- **Gradient Accents**: Blue to indigo gradients for primary actions
- **Smooth Animations**: 200-300ms transitions with hover effects
- **Mobile-First**: Responsive design with touch-friendly interactions
- **Accessibility**: High contrast and clear visual hierarchy

---

## 🎨 Color Palette

### Primary Colors
```css
/* Blue Gradient (Primary Brand) */
bg-gradient-to-r from-blue-600 to-indigo-600
bg-gradient-to-r from-blue-500 to-indigo-500
bg-gradient-to-r from-blue-700 to-indigo-700

/* Text Gradients */
bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent
```

### Semantic Colors
```css
/* Success, Warning, Error, Info */
text-green-600, bg-green-50, border-green-200
text-yellow-600, bg-yellow-50, border-yellow-200
text-red-600, bg-red-50, border-red-200
text-blue-600, bg-blue-50, border-blue-200
```

### Neutral Colors
```css
/* Backgrounds & Text */
bg-white/80 (glass morphism)
bg-gray-50, bg-gray-100, bg-gray-200
text-gray-900 (primary), text-gray-700 (secondary), text-gray-600 (tertiary)
```

---

## 📝 Typography

### Font Weights (Montserrat)
```css
.montserrat-thin { font-weight: 100; }
.montserrat-light { font-weight: 300; }
.montserrat-regular { font-weight: 400; }
.montserrat-medium { font-weight: 500; }
.montserrat-semibold { font-weight: 600; }
.montserrat-bold { font-weight: 700; }
.montserrat-black { font-weight: 900; }
```

### Text Hierarchy
```css
/* Headings */
text-4xl font-bold montserrat-bold (H1)
text-3xl font-bold montserrat-bold (H2)
text-2xl font-bold montserrat-bold (H3)
text-xl font-semibold montserrat-semibold (H4)

/* Body Text */
text-lg font-medium montserrat-medium (Large)
text-base font-medium montserrat-medium (Standard)
text-sm font-medium montserrat-medium (Small)
```

---

## 👤 Profile Page Hierarchy Standard

### Page Structure & Layout
```css
/* Main Container */
min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50
mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8

/* Content Wrapper */
mx-auto max-w-4xl (for focused content)
mx-auto max-w-7xl (for full-width content)
```

### Header Section
```css
/* Page Title */
text-2xl sm:text-3xl lg:text-4xl font-bold montserrat-bold
bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent

/* Page Subtitle */
text-sm sm:text-base font-medium text-gray-600

/* Back Button */
inline-flex items-center px-4 py-2 text-sm font-medium text-blue-600 
hover:text-blue-700 hover:bg-blue-50 rounded-lg transition-all duration-200
```

### Section Headers
```css
/* Primary Section Title */
text-xl font-semibold montserrat-semibold text-gray-900

/* Secondary Section Title */
text-lg font-semibold montserrat-semibold text-gray-900

/* Section Subtitle */
text-sm font-medium text-gray-600
```

### Card Components
```css
/* Main Card */
bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg border border-gray-200/50 p-6 sm:p-8
transition-all duration-300 hover:shadow-2xl

/* Card Header */
mb-6 flex items-center justify-between

/* Card Title */
text-xl font-semibold montserrat-semibold text-gray-900
```

### Information Display
```css
/* Field Labels */
text-sm font-medium text-gray-700 (primary labels)
text-sm font-medium text-gray-500 (secondary labels)

/* Field Values */
text-gray-900 (primary values)
text-gray-600 (secondary values)

/* Status Badges */
inline-flex rounded-full px-2 py-1 text-xs font-semibold
bg-green-100 text-green-800 (success)
bg-red-100 text-red-800 (error)
bg-yellow-100 text-yellow-800 (warning)
bg-blue-100 text-blue-800 (info)
```

### Action Buttons
```css
/* Primary Action */
px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 
hover:from-blue-700 hover:to-indigo-700 text-white font-semibold rounded-lg
transition-all duration-200 hover:shadow-lg transform hover:-translate-y-0.5

/* Secondary Action */
px-6 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium rounded-lg
transition-all duration-200 hover:shadow-md

/* Danger Action */
px-6 py-3 bg-gradient-to-r from-red-600 to-red-700 
hover:from-red-700 hover:to-red-800 text-white font-semibold rounded-lg
transition-all duration-200 hover:shadow-lg transform hover:-translate-y-0.5
```

### Form Elements
```css
/* Input Fields */
w-full bg-white border border-gray-200 rounded-lg px-4 py-3 
focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200

/* Labels */
text-sm font-medium text-gray-700 mb-2

/* Error States */
border-red-300 focus:ring-red-500 focus:border-red-500 text-red-600
```

### Loading States
```css
/* Loading Spinner */
mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-200 border-t-blue-600

/* Loading Text */
text-lg font-medium text-gray-600

/* Loading Container */
py-12 text-center
```

### Empty States
```css
/* Empty State Container */
rounded-xl border border-gray-200 bg-gray-50 p-8 text-center

/* Empty State Icon */
text-6xl mb-4

/* Empty State Title */
text-lg font-medium text-gray-900 mb-2

/* Empty State Description */
text-gray-600 mb-4
```

### Grid Layouts
```css
/* Information Grid */
grid grid-cols-1 gap-6 md:grid-cols-2

/* Card Grid */
grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3

/* Stats Grid */
grid grid-cols-3 gap-6
```

### Spacing System
```css
/* Section Spacing */
space-y-8 (between major sections)
mb-8 sm:mb-12 (header sections)

/* Card Spacing */
p-6 sm:p-8 (card padding)
mb-6 (card header margin)
space-y-4 (content spacing)

/* Form Spacing */
space-y-6 (form sections)
gap-6 (form grid)
```

### Responsive Breakpoints
```css
/* Mobile First */
text-sm, p-4, space-y-4 (mobile: 320px - 639px)
sm:text-base, sm:p-6, sm:space-y-6 (sm: 640px+)
md:text-lg, md:p-8, md:space-y-8 (md: 768px+)
lg:text-xl, lg:p-12, lg:space-y-12 (lg: 1024px+)
xl:text-2xl, xl:p-16, xl:space-y-16 (xl: 1280px+)
```

---

## 📐 Spacing System

```css
/* Standard Scale */
p-2, m-2 (8px) - Minimal
p-4, m-4 (16px) - Standard
p-6, m-6 (24px) - Medium
p-8, m-8 (32px) - Large
p-12, m-12 (48px) - Extra large
p-16, m-16 (64px) - Section

/* Component Spacing */
space-x-4, space-y-4 (16px) - Standard
space-x-6, space-y-6 (24px) - Comfortable
space-x-8, space-y-8 (32px) - Loose
```

---

## 🧩 Component Patterns

### Cards
```css
/* Standard Card */
bg-white rounded-2xl shadow-lg hover:shadow-2xl 
transition-all duration-300 transform hover:-translate-y-2 
border border-gray-100 p-8

/* Glass Card */
bg-white/80 backdrop-blur-xl rounded-2xl shadow-lg 
border border-gray-200/50 p-8

/* Interactive Card */
group cursor-pointer
hover:shadow-2xl hover:-translate-y-2
transition-all duration-300
```

### Buttons
```css
/* Primary Button */
bg-gradient-to-r from-blue-600 to-indigo-600 
hover:from-blue-700 hover:to-indigo-700
text-white font-semibold rounded-lg
px-6 py-3 transition-all duration-200
hover:shadow-lg transform hover:-translate-y-0.5

/* Secondary Button */
bg-gray-100 hover:bg-gray-200
text-gray-700 font-medium rounded-lg
px-4 py-2 transition-all duration-200
hover:shadow-md

/* Icon Button */
w-12 h-12 rounded-full flex items-center justify-center
bg-gradient-to-r from-blue-500 to-indigo-600
hover:scale-110 transition-transform duration-300
```

### Navigation
```css
/* Active State */
text-blue-600 bg-blue-50 border border-blue-200
rounded-lg px-4 py-2

/* Hover State */
text-gray-700 hover:text-blue-600 hover:bg-blue-50
rounded-lg px-4 py-2 transition-all duration-200
```

### Form Elements
```css
/* Input Fields */
bg-white border border-gray-200 rounded-lg
px-4 py-3 focus:ring-2 focus:ring-blue-500 
focus:border-blue-500 transition-all duration-200

/* Labels */
text-sm font-medium text-gray-700 mb-2

/* Error States */
border-red-300 focus:ring-red-500 focus:border-red-500
text-red-600
```

---

## 🎭 Animation Guidelines

### Transitions
```css
/* Durations */
transition-all duration-150 (fast)
transition-all duration-200 (standard)
transition-all duration-300 (smooth)
transition-all duration-500 (slow)

/* Effects */
hover:scale-105 (5% scale)
hover:scale-110 (10% scale)
hover:-translate-y-1 (subtle lift)
hover:-translate-y-2 (medium lift)
hover:shadow-lg (standard shadow)
hover:shadow-2xl (extra large shadow)
```

### Loading States
```css
/* Skeleton */
animate-pulse bg-gray-200 rounded

/* Spinner */
animate-spin w-6 h-6 border-2 border-blue-600 
border-t-transparent rounded-full
```

---

## 📱 Mobile-First Responsive Design

### Breakpoint Strategy
```css
/* Mobile First - Start with mobile styles */
text-sm, p-4, space-y-4, gap-4 (mobile: 320px - 639px)
sm:text-base, sm:p-6, sm:space-y-6, sm:gap-6 (sm: 640px+)
md:text-lg, md:p-8, md:space-y-8, md:gap-8 (md: 768px+)
lg:text-xl, lg:p-12, lg:space-y-12, lg:gap-8 (lg: 1024px+)
xl:text-2xl, xl:p-16, xl:space-y-16, xl:gap-12 (xl: 1280px+)
```

### Mobile Patterns
```css
/* Touch-Friendly Elements */
min-h-[44px] min-w-[44px] (minimum touch target)
px-4 py-3 text-base (mobile buttons)
sm:px-6 sm:py-3 sm:text-sm (tablet+ buttons)

/* Mobile Navigation */
md:hidden (mobile menu button)
fixed inset-y-0 right-0 z-50 w-80 bg-white shadow-xl
transform translate-x-full transition-transform duration-300

/* Mobile Typography */
text-sm (mobile base)
sm:text-base (tablet+ base)
text-2xl font-bold (mobile H1)
sm:text-3xl (tablet H1)
lg:text-4xl (desktop H1)
```

### Responsive Grids
```css
/* Card Grids */
grid grid-cols-1 gap-4 (mobile - single column)
sm:grid-cols-2 sm:gap-6 (tablet - 2 columns)
lg:grid-cols-3 lg:gap-8 (desktop - 3 columns)
xl:grid-cols-4 xl:gap-8 (large desktop - 4 columns)

/* Layout Containers */
max-w-full px-4 mx-auto (mobile)
sm:max-w-2xl sm:px-6 (tablet)
lg:max-w-4xl lg:px-8 (desktop)
xl:max-w-7xl xl:px-8 (large desktop)
```

### Mobile Navigation & Overlays
```css
/* Glass Morphism Backdrop */
.bg-black/20 backdrop-blur-sm (modern backdrop)
.bg-black/30 backdrop-blur-md (enhanced backdrop)

/* Smooth Slide Animations */
transform transition-transform duration-300 ease-in-out
translate-x-0 (slide in)
translate-x-full (slide out)

/* Z-Index Hierarchy */
.navbar-base { z-index: 50; }
.mobile-overlay { z-index: 40; }
.mobile-menu-panel { z-index: 50; }
.modal-backdrop { z-index: 30; }
.modal-content { z-index: 40; }
```

---

## 🎨 Layout Patterns

### Page Structure
```css
/* Main Container */
min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50

/* Content Wrapper */
mx-auto max-w-7xl px-4 sm:px-6 lg:px-8

/* Section Spacing */
py-16 sm:py-20 lg:py-24
```

### Hero Sections
```css
/* Hero Container */
relative overflow-hidden py-16 sm:py-20 lg:py-24

/* Background Effects */
absolute inset-0 bg-gradient-to-r from-blue-600 to-indigo-600 opacity-10

/* Content Positioning */
relative z-10 text-center
```

---

## 🔧 Utility Classes

### Common Combinations
```css
/* Glass morphism container */
bg-white/80 backdrop-blur-xl border border-gray-200/50 shadow-lg

/* Gradient text */
bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent

/* Interactive element */
cursor-pointer hover:scale-105 transition-transform duration-300

/* Status indicator */
inline-flex items-center px-3 py-1 rounded-full text-sm font-medium
```

### Custom Utilities
```css
/* Add to your CSS file for repeated patterns */
.glass-card {
  @apply bg-white/80 backdrop-blur-xl border border-gray-200/50 shadow-lg;
}

.gradient-text {
  @apply bg-gradient-to-r from-gray-900 to-gray-600 bg-clip-text text-transparent;
}

.interactive-card {
  @apply group cursor-pointer hover:shadow-2xl hover:-translate-y-2 transition-all duration-300;
}
```

---

## 🎯 Component Examples

### Feature Card
```html
<div class="bg-white rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 p-8 border border-gray-100">
  <div class="w-16 h-16 bg-gradient-to-r from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
    <!-- Icon here -->
  </div>
  <h3 class="text-xl font-semibold text-gray-900 mb-3 montserrat-semibold">Title</h3>
  <p class="text-gray-600 mb-4">Description</p>
  <div class="flex items-center text-blue-600 font-medium">
    <span>Action</span>
    <svg class="w-4 h-4 ml-2 group-hover:translate-x-1 transition-transform duration-300">
      <!-- Arrow icon -->
    </svg>
  </div>
</div>
```

### Status Badge
```html
<span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
  <svg class="w-4 h-4 mr-2">
    <!-- Check icon -->
  </svg>
  Active
</span>
```

---

## 🚀 Implementation Checklist

1. **✅ Use established color palette**
2. **✅ Apply consistent spacing (p-4, p-8, etc.)**
3. **✅ Use Montserrat font family with appropriate weights**
4. **✅ Include hover states and transitions**
5. **✅ Make it responsive (mobile-first)**
6. **✅ Add glass morphism effects where appropriate**
7. **✅ Use gradient accents for primary actions**
8. **✅ Ensure accessibility (contrast, focus states)**
9. **✅ Test on different screen sizes**
10. **✅ Maintain visual hierarchy**

---

## 📚 Resources

- **Tailwind CSS Documentation**: https://tailwindcss.com/docs
- **Color Palette Generator**: https://coolors.co
- **Font Pairing**: https://fontpair.co
- **Animation Inspiration**: https://animate.style

---

*This design system should be treated as a living document. Update it as new patterns emerge and the design evolves.*