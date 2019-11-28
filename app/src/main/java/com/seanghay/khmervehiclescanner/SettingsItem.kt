package com.seanghay.khmervehiclescanner

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


abstract class SettingsItem(
    open var id: Int,

    @DrawableRes
    open var iconsRes: Int,

    @StringRes
    open var titleRes: Int
)

data class ThemeSettingsItem(
    override var id: Int,

    @DrawableRes
    override var iconsRes: Int,

    @StringRes
    override var titleRes: Int,

    var isLight: Boolean
) : SettingsItem(id, iconsRes, titleRes)

data class LocaleSettingsItem(
    override var id: Int,

    @DrawableRes
    override var iconsRes: Int,

    @StringRes
    override var titleRes: Int,

    var locale: String? = null
) : SettingsItem(id, iconsRes, titleRes)


data class AboutSettingsItems(
    override var id: Int,

    @DrawableRes
    override var iconsRes: Int,

    @StringRes
    override var titleRes: Int
) : SettingsItem(id, iconsRes, titleRes)
