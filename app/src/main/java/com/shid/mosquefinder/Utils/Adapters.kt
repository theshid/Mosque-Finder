package com.shid.mosquefinder.Utils

import com.shid.mosquefinder.Data.Model.Article
import me.ibrahimyilmaz.kiel.adapterOf


internal inline fun createBlogAdapter()= adapterOf<Article> {
    diff (
        areContentsTheSame = { old, new -> old == new },
        areItemsTheSame = { old, new -> old.body == new.body }
            )

}