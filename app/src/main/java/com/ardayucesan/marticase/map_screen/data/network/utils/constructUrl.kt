package com.ardayucesan.marticase.map_screen.data.network.utils

import com.ardayucesan.marticase.BuildConfig

fun constructUrl(url: String): String {
    return when {
        url.contains(BuildConfig.ROUTES_BASE_URL) -> url;
        url.startsWith("/") -> BuildConfig.ROUTES_BASE_URL + url.drop(1);
        else -> BuildConfig.ROUTES_BASE_URL + url;
    }
}