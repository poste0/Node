package ru.descriptor.dynamicParam;

import ru.data.UserData;
import ru.data.VideoData;

public interface DynamicParamValueProvider {
    String provide(UserData userData, VideoData videoData);
}
