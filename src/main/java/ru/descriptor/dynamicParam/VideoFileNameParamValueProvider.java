package ru.descriptor.dynamicParam;

import ru.data.UserData;
import ru.data.VideoData;

public class VideoFileNameParamValueProvider implements DynamicParamValueProvider {
    @Override
    public String provide(UserData userData, VideoData videoData) {
        return videoData.getVideoFile().getPath();
    }
}
