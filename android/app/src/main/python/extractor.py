import json
from pytubefix import YouTube

def extract_res_val(res_str):
    if not res_str: return 0
    return int(''.join(filter(str.isdigit, str(res_str))))

def extract_abr_val(abr_str):
    if not abr_str: return 0
    return int(''.join(filter(str.isdigit, str(abr_str))))

def format_size(bytes_size):
    if not bytes_size:
        return "Unknown Size"
    try:
        bytes_size = float(bytes_size)
    except (ValueError, TypeError):
        return "Unknown Size"
        
    for unit in ['B', 'KB', 'MB', 'GB']:
        if bytes_size < 1024.0:
            return f"{bytes_size:.1f} {unit}"
        bytes_size /= 1024.0
    return f"{bytes_size:.1f} TB"

def safe_format_name(mime_type):
    if not mime_type or not isinstance(mime_type, str):
        return "UNKNOWN"
    
    mime_type = mime_type.strip().lower()
    if 'mp4' in mime_type:
        return "MP4"
    elif 'webm' in mime_type:
        return "WEBM"
    elif 'm4a' in mime_type or 'mp4a' in mime_type:
        return "M4A"
    
    parts = mime_type.split('/')
    if len(parts) > 1:
        return parts[1].upper()
    return mime_type.upper()

def fetch_streams_json(url):
    try:
        yt = YouTube(url)
        
        # Metadata
        video_info = {
            "title": getattr(yt, 'title', 'Unknown Title') or "Unknown Title",
            "author": getattr(yt, 'author', 'Unknown Author') or "Unknown Author",
            "length": getattr(yt, 'length', 0),
            "views": getattr(yt, 'views', 0),
            "publish_date": str(getattr(yt, 'publish_date', None)),
            "thumbnail_url": getattr(yt, 'thumbnail_url', None)
        }

        # Video Streams
        video_streams = yt.streams.filter(type="video")
        video_list = list(video_streams)
        video_list.sort(key=lambda s: extract_res_val(getattr(s, 'resolution', '')), reverse=True)
        
        unique_videos = []
        seen_video_res = set()
        for s in video_list:
            res = getattr(s, 'resolution', None)
            if res and res not in seen_video_res:
                seen_video_res.add(res)
                unique_videos.append({
                    "itag": str(getattr(s, 'itag', '')),
                    "mime_type": getattr(s, 'mime_type', ""),
                    "format": safe_format_name(getattr(s, 'mime_type', "")),
                    "filesize": getattr(s, 'filesize', 0),
                    "filesize_str": format_size(getattr(s, 'filesize', 0)),
                    "resolution": res,
                    "fps": str(getattr(s, 'fps', 'N/A')),
                    "codec": getattr(s, 'video_codec', 'N/A')
                })

        # Audio Streams
        audio_streams = yt.streams.filter(only_audio=True)
        audio_list = list(audio_streams)
        audio_list.sort(key=lambda s: extract_abr_val(getattr(s, 'abr', '')), reverse=True)
        
        unique_audios = []
        seen_audio_abr = set()
        for s in audio_list:
            abr = getattr(s, 'abr', None)
            if abr and abr not in seen_audio_abr:
                seen_audio_abr.add(abr)
                unique_audios.append({
                    "itag": str(getattr(s, 'itag', '')),
                    "mime_type": getattr(s, 'mime_type', ""),
                    "format": safe_format_name(getattr(s, 'mime_type', "")),
                    "filesize": getattr(s, 'filesize', 0),
                    "filesize_str": format_size(getattr(s, 'filesize', 0)),
                    "abr": abr,
                    "codec": getattr(s, 'audio_codec', 'N/A')
                })

        payload = {
            "success": True,
            "video_info": video_info,
            "video": unique_videos,
            "audio": unique_audios
        }
        return json.dumps(payload)
        
    except Exception as e:
        return json.dumps({
            "success": False,
            "error": str(e)
        })
