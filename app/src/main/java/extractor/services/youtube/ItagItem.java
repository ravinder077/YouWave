package extractor.services.youtube;

import extractor.MediaFormat;
import extractor.exceptions.ParsingException;

import static extractor.MediaFormat.*;
import static extractor.MediaFormat.M4A;
import static extractor.MediaFormat.MPEG_4;
import static extractor.MediaFormat.WEBM;
import static extractor.MediaFormat.WEBMA;
import static extractor.MediaFormat.v3GPP;
import static extractor.services.youtube.ItagItem.ItagType.*;

public class ItagItem {
    /**
     * List can be found here https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py#L360
     */
    private static final ItagItem[] ITAG_LIST = {
            /////////////////////////////////////////////////////
            // VIDEO     ID  Type   Format  Resolution  FPS  ///
            ///////////////////////////////////////////////////
            new ItagItem(17, ItagType.VIDEO, v3GPP, "144p"),
            new ItagItem(36, ItagType.VIDEO, v3GPP, "240p"),

            new ItagItem(18, ItagType.VIDEO, MPEG_4, "360p"),
            new ItagItem(34, ItagType.VIDEO, MPEG_4, "360p"),
            new ItagItem(35, ItagType.VIDEO, MPEG_4, "480p"),
            new ItagItem(59, ItagType.VIDEO, MPEG_4, "480p"),
            new ItagItem(78, ItagType.VIDEO, MPEG_4, "480p"),
            new ItagItem(22, ItagType.VIDEO, MPEG_4, "720p"),
            new ItagItem(37, ItagType.VIDEO, MPEG_4, "1080p"),
            new ItagItem(38, ItagType.VIDEO, MPEG_4, "1080p"),

            new ItagItem(43, ItagType.VIDEO, WEBM, "360p"),
            new ItagItem(44, ItagType.VIDEO, WEBM, "480p"),
            new ItagItem(45, ItagType.VIDEO, WEBM, "720p"),
            new ItagItem(46, ItagType.VIDEO, WEBM, "1080p"),

            ////////////////////////////////////////////////////////////////////
            // AUDIO     ID      ItagType          Format        Bitrate    ///
            //////////////////////////////////////////////////////////////////
            // Disable Opus codec as it's not well supported in older devices
//          new ItagItem(249, AUDIO, WEBMA, 50),
//          new ItagItem(250, AUDIO, WEBMA, 70),
//          new ItagItem(251, AUDIO, WEBMA, 160),
            new ItagItem(171, ItagType.AUDIO, WEBMA, 128),
            new ItagItem(172, ItagType.AUDIO, WEBMA, 256),
            new ItagItem(139, ItagType.AUDIO, M4A, 48),
            new ItagItem(140, ItagType.AUDIO, M4A, 128),
            new ItagItem(141, ItagType.AUDIO, M4A, 256),

            /// VIDEO ONLY ////////////////////////////////////////////
            //           ID      Type     Format  Resolution  FPS  ///
            /////////////////////////////////////////////////////////
            // Don't add VideoOnly streams that have normal variants
            new ItagItem(160, ItagType.VIDEO_ONLY, MPEG_4, "144p"),
            new ItagItem(133, ItagType.VIDEO_ONLY, MPEG_4, "240p"),
//          new ItagItem(134, VIDEO_ONLY, MPEG_4, "360p"),
            new ItagItem(135, ItagType.VIDEO_ONLY, MPEG_4, "480p"),
            new ItagItem(212, ItagType.VIDEO_ONLY, MPEG_4, "480p"),
//          new ItagItem(136, VIDEO_ONLY, MPEG_4, "720p"),
            new ItagItem(298, ItagType.VIDEO_ONLY, MPEG_4, "720p60", 60),
            new ItagItem(137, ItagType.VIDEO_ONLY, MPEG_4, "1080p"),
            new ItagItem(299, ItagType.VIDEO_ONLY, MPEG_4, "1080p60", 60),
            new ItagItem(266, ItagType.VIDEO_ONLY, MPEG_4, "2160p"),

            new ItagItem(278, ItagType.VIDEO_ONLY, WEBM, "144p"),
            new ItagItem(242, ItagType.VIDEO_ONLY, WEBM, "240p"),
//          new ItagItem(243, VIDEO_ONLY, WEBM, "360p"),
            new ItagItem(244, ItagType.VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(245, ItagType.VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(246, ItagType.VIDEO_ONLY, WEBM, "480p"),
            new ItagItem(247, ItagType.VIDEO_ONLY, WEBM, "720p"),
            new ItagItem(248, ItagType.VIDEO_ONLY, WEBM, "1080p"),
            new ItagItem(271, ItagType.VIDEO_ONLY, WEBM, "1440p"),
            // #272 is either 3840x2160 (e.g. RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            new ItagItem(272, ItagType.VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(302, ItagType.VIDEO_ONLY, WEBM, "720p60", 60),
            new ItagItem(303, ItagType.VIDEO_ONLY, WEBM, "1080p60", 60),
            new ItagItem(308, ItagType.VIDEO_ONLY, WEBM, "1440p60", 60),
            new ItagItem(313, ItagType.VIDEO_ONLY, WEBM, "2160p"),
            new ItagItem(315, ItagType.VIDEO_ONLY, WEBM, "2160p60", 60)
    };

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    public static boolean isSupported(int itag) {
        for (ItagItem item : ITAG_LIST) {
            if (itag == item.id) {
                return true;
            }
        }
        return false;
    }

    public static ItagItem getItag(int itagId) throws ParsingException {
        for (ItagItem item : ITAG_LIST) {
            if (itagId == item.id) {
                return item;
            }
        }
        throw new ParsingException("itag=" + Integer.toString(itagId) + " not supported");
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contructors and misc
    //////////////////////////////////////////////////////////////////////////*/

    public enum ItagType {
        AUDIO,
        VIDEO,
        VIDEO_ONLY
    }

    /**
     * Call {@link #ItagItem(int, ItagType, MediaFormat, String, int)} with the fps set to 30.
     */
    public ItagItem(int id, ItagType type, MediaFormat format, String resolution) {
        this.id = id;
        this.itagType = type;
        this.mediaFormatId = format.id;
        this.resolutionString = resolution;
        this.fps = 30;
    }

    /**
     * Constructor for videos.
     *
     * @param resolution string that will be used in the frontend
     */
    public ItagItem(int id, ItagType type, MediaFormat format, String resolution, int fps) {
        this.id = id;
        this.itagType = type;
        this.mediaFormatId = format.id;
        this.resolutionString = resolution;
        this.fps = fps;
    }

    public ItagItem(int id, ItagType type, MediaFormat format, int avgBitrate) {
        this.id = id;
        this.itagType = type;
        this.mediaFormatId = format.id;
        this.avgBitrate = avgBitrate;
    }

    public int id;
    public ItagType itagType;
    public int mediaFormatId;

    // Audio fields
    public int avgBitrate = -1;

    // Video fields
    public String resolutionString;
    public int fps = -1;

}
