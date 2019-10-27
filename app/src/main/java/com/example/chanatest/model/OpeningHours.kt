package com.example.chanatest.model

import com.google.gson.annotations.SerializedName

data class OpeningHours(

	@field:SerializedName("open_now")
	val openNow: Boolean? = null
)