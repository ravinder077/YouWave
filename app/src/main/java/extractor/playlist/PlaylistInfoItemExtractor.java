package extractor.playlist;

import extractor.InfoItemExtractor;
import extractor.exceptions.ParsingException;

public interface PlaylistInfoItemExtractor extends InfoItemExtractor {
    String getUploaderName() throws ParsingException;
    long getStreamCount() throws ParsingException;
}
