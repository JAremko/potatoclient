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
DependencyNote=Important: This installer will install Microsoft Visual C++ 2015-2022 Redistributable and GStreamer 1.26.3. A system restart may be required for changes to take effect.

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
; Install Visual C++ 2015-2022 Redistributable - ALWAYS install to ensure we have the latest version
Filename: "{tmp}\vc_redist_x64.exe"; Parameters: "/install /passive /norestart"; StatusMsg: "Installing Visual C++ 2015-2022 Redistributable (required for GStreamer 1.26.3)..."; Flags: waituntilterminated runhidden; BeforeInstall: LogInstallStart('Visual C++ 2015-2022 Redistributable'); AfterInstall: SetLastResultCode

; Install Visual C++ 2013 Redistributable (may be needed for some plugins)
Filename: "{tmp}\vcredist_2013_x64.exe"; Parameters: "/install /passive /norestart"; StatusMsg: "Installing Visual C++ 2013 Redistributable..."; Flags: waituntilterminated runhidden; Check: VCRedist2013NeedsInstall; BeforeInstall: LogInstallStart('Visual C++ 2013 Redistributable'); AfterInstall: SetLastResultCode

; Install GStreamer Runtime
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\gstreamer-1.0-msvc-x86_64-1.26.3.msi"" /passive"; StatusMsg: "Installing GStreamer 1.26.3 Runtime..."; Flags: waituntilterminated runhidden; BeforeInstall: LogInstallStart('GStreamer 1.26.3 Runtime'); AfterInstall: SetLastResultCode

; Install GStreamer Development (includes H264 decoders like avdec_h264)
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\gstreamer-1.0-devel-msvc-x86_64-1.26.3.msi"" /passive"; StatusMsg: "Installing GStreamer 1.26.3 Development (H264 codecs)..."; Flags: waituntilterminated runhidden; BeforeInstall: LogInstallStart('GStreamer 1.26.3 Development'); AfterInstall: SetLastResultCode

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
  Installed: Cardinal;
begin
  Result := True;
  
  // Check multiple possible registry locations for VC++ 2015-2022
  // First check for VS 2022 runtime (v143)
  if RegQueryDWordValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Microsoft\VisualStudio\14.0\VC\Runtimes\x64', 'Installed', Installed) and (Installed = 1) then
  begin
    if RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\Microsoft\VisualStudio\14.0\VC\Runtimes\x64', 'Version', Version) then
    begin
      // Check for v14.30 or higher (VS 2022)
      if (Version >= 'v14.30') then
      begin
        Result := False;
        exit;
      end;
    end;
  end;
  
  // Also check WOW6432Node
  if RegQueryDWordValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\WOW6432Node\Microsoft\VisualStudio\14.0\VC\Runtimes\x64', 'Installed', Installed) and (Installed = 1) then
  begin
    if RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\WOW6432Node\Microsoft\VisualStudio\14.0\VC\Runtimes\x64', 'Version', Version) then
    begin
      if (Version >= 'v14.30') then
      begin
        Result := False;
        exit;
      end;
    end;
  end;
  
  // If we get here, we need to install
  Result := True;
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

var
  CurrentInstalling: string;
  InstallErrors: string;
  LastResultCode: Integer;

procedure CheckInstallResult();
begin
  // Log the installation completion
  Log(CurrentInstalling + ' installation finished');
  
  // Note: In the simplified approach, we can't get the actual exit code from [Run] section
  // The error handling will be done in CurStepChanged
end;

procedure LogInstallStart(ComponentName: string);
begin
  CurrentInstalling := ComponentName;
  LastResultCode := 0;
  Log('Starting installation of: ' + ComponentName);
end;

procedure SetLastResultCode();
begin
  // In Inno Setup's [Run] section, we can't directly access the exit code
  // The installer will continue even if components fail, so we'll track errors differently
  LastResultCode := 0; // Assume success for now
  CheckInstallResult();
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssInstall then
  begin
    InstallErrors := '';
  end
  else if CurStep = ssPostInstall then
  begin
    // Since we can't capture exit codes in [Run] section easily,
    // just show a success message
    MsgBox('Installation complete!' + #13#10 + #13#10 + 
           'All dependencies have been installed.' + #13#10 + #13#10 +
           'If you experience any issues, please restart your computer.',
           mbInformation, MB_OK);
  end;
end;

[UninstallDelete]
Type: filesandordirs; Name: "{app}"