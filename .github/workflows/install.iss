; InnoSetup script for Windows installer

#define MyAppName "PotatoClient"
#define MyAppExeName "PotatoClient.exe"
#define MyAppPublisher "PotatoClient Project"
#define MyAppURL "https://github.com/yourusername/potatoclient"

[Setup]
AppId={{A7B4F8C2-5D9E-4A3B-8C1D-E2F3A4B5C6D7}
AppName={#MyAppName}
AppVersion={#GetEnv('APP_VERSION')}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputDir=..\..\Output
OutputBaseFilename={#MyAppName}-{#GetEnv('APP_VERSION')}-setup
Compression=lzma2
SolidCompression=yes
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64
MinVersion=6.1.7601
DisableProgramGroupPage=yes
DisableWelcomePage=no
DisableDirPage=no
DisableReadyPage=no
CreateAppDir=yes
UninstallDisplayIcon={app}\{#MyAppExeName}
SetupIconFile=..\..\resources\icon.ico
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[CustomMessages]
AdminPrivilegesInfo=This installer requires administrator privileges to install system dependencies (Visual C++ redistributables and GStreamer). The installer will request elevation when you click Next.

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 6.1

[Files]
; Main application files
Source: "..\..\dist\PotatoClient\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\dist\PotatoClient\potatoclient.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\dist\PotatoClient\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\dist\PotatoClient\resources\*"; DestDir: "{app}\resources"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\dist\PotatoClient\version.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\dist\PotatoClient\README-DEPENDENCIES.txt"; DestDir: "{app}"; Flags: ignoreversion isreadme

; Redistributables (to temp for installation)
Source: "..\..\dist\PotatoClient\redist\vc_redist_x64.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "..\..\dist\PotatoClient\redist\vcredist_2013_x64.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "..\..\dist\PotatoClient\redist\gstreamer-1.0-msvc-x86_64-1.26.3.msi"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "..\..\dist\PotatoClient\redist\gstreamer-1.0-devel-msvc-x86_64-1.26.3.msi"; DestDir: "{tmp}"; Flags: deleteafterinstall

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
; Install Visual C++ 2015-2022 Redistributable
Filename: "{tmp}\vc_redist_x64.exe"; Parameters: "/quiet /norestart"; StatusMsg: "Installing Visual C++ 2015-2022 Redistributable..."; Flags: waituntilterminated; Check: VCRedist2015NeedsInstall

; Install Visual C++ 2013 Redistributable (may be needed for some plugins)
Filename: "{tmp}\vcredist_2013_x64.exe"; Parameters: "/quiet /norestart"; StatusMsg: "Installing Visual C++ 2013 Redistributable..."; Flags: waituntilterminated; Check: VCRedist2013NeedsInstall

; Install GStreamer Runtime
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\gstreamer-1.0-msvc-x86_64-1.26.3.msi"" /qb"; StatusMsg: "Installing GStreamer 1.26.3 Runtime..."; Flags: waituntilterminated

; Install GStreamer Development (includes H264 decoders like avdec_h264)
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\gstreamer-1.0-devel-msvc-x86_64-1.26.3.msi"" /qb"; StatusMsg: "Installing GStreamer 1.26.3 Development (H264 codecs)..."; Flags: waituntilterminated

; Launch application after install
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Registry]
; Add to PATH for GStreamer (system-level since we have admin rights)
; Note: GStreamer 1.26+ installs to Program Files by default
Root: HKLM; Subkey: "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"; ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{pf}\gstreamer\1.0\msvc_x86_64\bin"; Check: NeedsAddPath('{pf}\gstreamer\1.0\msvc_x86_64\bin')

[Code]
function NeedsAddPath(Param: string): boolean;
var
  OrigPath: string;
begin
  if not RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'Path', OrigPath) then
  begin
    Result := True;
    exit;
  end;
  Result := Pos(';' + Param + ';', ';' + OrigPath + ';') = 0;
end;

function VCRedist2015NeedsInstall: Boolean;
var
  Version: String;
begin
  Result := True;
  if RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Microsoft\VisualStudio\14.0\VC\Runtimes\x64', 'Version', Version) or
     RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\WOW6432Node\Microsoft\VisualStudio\14.0\VC\Runtimes\x64', 'Version', Version) then
  begin
    // Version string format: v14.xx.xxxxx.xx
    if (Version >= 'v14.29') then
      Result := False;
  end;
end;

function VCRedist2013NeedsInstall: Boolean;
var
  Installed: Cardinal;
begin
  Result := True;
  if RegQueryDWordValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Microsoft\VisualStudio\12.0\VC\Runtimes\x64', 'Installed', Installed) or
     RegQueryDWordValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\WOW6432Node\Microsoft\VisualStudio\12.0\VC\Runtimes\x64', 'Installed', Installed) then
  begin
    if (Installed = 1) then
      Result := False;
  end;
end;

[UninstallDelete]
Type: filesandordirs; Name: "{app}"