"""Install a checksummed JMeter distribution on the native GitHub runner."""
import hashlib
import os
from pathlib import Path
import re
import tarfile
from urllib.request import urlopen


def main():
    folder = Path(os.environ['RUNNER_TEMP']) / 'native-jmeter'
    folder.mkdir(exist_ok=True)
    base = 'https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.tgz'
    with urlopen(base + '.sha512', timeout=60) as response:
        matches = re.findall(r'\b[a-fA-F0-9]{128}\b', response.read().decode())
    if len(matches) != 1:
        raise RuntimeError('Unexpected Apache checksum format')
    archive = folder / 'jmeter.tgz'
    with urlopen(base, timeout=60) as response, archive.open('wb') as output:
        while chunk := response.read(1024 * 1024):
            output.write(chunk)
    if hashlib.sha512(archive.read_bytes()).hexdigest().lower() != matches[0].lower():
        raise RuntimeError('Apache checksum mismatch')
    with tarfile.open(archive) as package:
        package.extractall(folder, filter='data')
    with open(os.environ['GITHUB_PATH'], 'a', encoding='utf-8') as output:
        output.write(str(folder / 'apache-jmeter-5.6.3/bin') + '\n')
    print('JMeter 5.6.3 installed; SHA-512 verified.')


if __name__ == '__main__':
    main()
