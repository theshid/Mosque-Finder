package com.shid.mosquefinder.app.utils

import com.shid.mosquefinder.data.model.Article
import me.ibrahimyilmaz.kiel.adapterOf


internal inline fun createBlogAdapter()= adapterOf<Article> {
    diff (
        areContentsTheSame = { old, new -> old == new },
        areItemsTheSame = { old, new -> old.body == new.body }
            )

}