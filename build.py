#!/usr/bin/env python3
"""Build offline using official Android SDK 36, build-tools 36 and JDK 21+."""
from pathlib import Path
import hashlib, os, secrets, subprocess, zipfile
ROOT = Path(__file__).resolve().parent
JDK = Path(os.environ.get('CHECKER_JDK', '/tmp/xiaomi-systemhelper-tools/jdk/Contents/Home'))
TOOLS = Path(os.environ.get('CHECKER_BUILD_TOOLS', '/tmp/xiaomi-apk-sdk/tools/android-16'))
ANDROID = Path(os.environ.get('CHECKER_ANDROID_JAR', '/tmp/xiaomi-apk-sdk/platform/android-36/android.jar'))
BUILD, DIST, SIGN = ROOT/'build', ROOT/'dist', ROOT/'signing'
for p in [BUILD, DIST, SIGN]: p.mkdir(exist_ok=True)
SIGN.chmod(0o700)
def run(*args): subprocess.run([str(a) for a in args], check=True)
password, key = SIGN/'password.txt', SIGN/'release.p12'
if not key.exists():
    if not password.exists():
        password.write_text(secrets.token_urlsafe(32)+'\n'); password.chmod(0o600)
    run(JDK/'bin/keytool','-genkeypair','-keystore',key,'-storetype','PKCS12','-storepass:file',password,'-keypass:file',password,'-alias','checker','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=SystemHelper Checker Local Build','-noprompt')
    key.chmod(0o600)

def build(name, source, manifest, test=False):
    work=BUILD/name
    work.mkdir(exist_ok=True)
    classes, dex=work/'classes', work/'dex'
    classes.mkdir(exist_ok=True); dex.mkdir(exist_ok=True)
    sources=sorted(source.rglob('*.java'))
    cp=str(ANDROID)+(os.pathsep+str(BUILD/'app/classes') if test else '')
    run(JDK/'bin/javac','--release','8','-Xlint:-options','-encoding','UTF-8','-cp',cp,'-d',classes,*sources)
    d8args=[JDK/'bin/java','-cp',TOOLS/'lib/d8.jar','com.android.tools.r8.D8','--min-api','26','--lib',ANDROID,'--output',dex]
    if test: d8args += ['--classpath', BUILD/'app/classes']
    run(*d8args,*sorted(classes.rglob('*.class')))
    unsigned=work/'unsigned.apk'
    link=[TOOLS/'aapt2','link','-I',ANDROID,'--manifest',manifest,'-o',unsigned]
    if not test:
        resources=work/'resources.zip'
        run(TOOLS/'aapt2','compile','--dir',ROOT/'res','-o',resources)
        link += [resources]
    run(*link)
    with zipfile.ZipFile(unsigned,'a',compression=zipfile.ZIP_DEFLATED) as z:
        z.write(dex/'classes.dex','classes.dex')
    aligned=work/'aligned.apk'
    run(TOOLS/'zipalign','-f','-p','4',unsigned,aligned)
    output=DIST/('checker-tests.apk' if test else 'SystemHelper-Checker-v1.0.apk')
    signer=[JDK/'bin/java','-jar',TOOLS/'lib/apksigner.jar']
    run(*signer,'sign','--ks',key,'--ks-key-alias','checker','--ks-pass','file:'+str(password),'--out',output,aligned)
    run(*signer,'verify','--verbose',output)
    print(output.name, 'SHA256',hashlib.sha256(output.read_bytes()).hexdigest())

build('app',ROOT/'src',ROOT/'AndroidManifest.xml')
if '--tests' in __import__('sys').argv:
    build('test',ROOT/'tests',ROOT/'tests/AndroidManifest.xml',test=True)
