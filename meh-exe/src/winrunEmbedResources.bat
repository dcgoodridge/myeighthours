RCEDIT /C meh.exe
RCEDIT /I meh.exe exe.ico
RCEDIT /N meh.exe meh.ini
RCEDIT /S meh.exe splash.jpg

RCEDIT64 /C meh64.exe
RCEDIT64 /I meh64.exe exe.ico
RCEDIT64 /N meh64.exe meh64.ini
RCEDIT64 /S meh64.exe splash.jpg

GoRC /fo Resources.res Resources.rc
GoRC /machine X64 /fo Resources64.res Resources.rc

ResourceHacker -add meh.exe, meh.exe, Resources.res,,,
ResourceHacker -add meh64.exe, meh64.exe, Resources64.res,,,