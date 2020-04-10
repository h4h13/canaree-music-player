package dev.olog.feature.detail.dagger

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dev.olog.feature.detail.DetailFragment
import dev.olog.feature.detail.DetailFragmentViewModel
import dev.olog.feature.presentation.base.dagger.ViewModelKey
import dev.olog.feature.presentation.base.extensions.getArgument
import dev.olog.feature.presentation.base.model.PresentationId

@Module
internal abstract class DetailFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DetailFragmentViewModel::class)
    abstract fun provideViewModel(viewModel: DetailFragmentViewModel): ViewModel

    companion object {

        @Provides
        internal fun provideMediaId(instance: DetailFragment): PresentationId.Category {
            return instance.getArgument(DetailFragment.ARGUMENTS_MEDIA_ID)
        }

    }


}