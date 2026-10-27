import csv
import json
from pathlib import Path


def main():
    folder = Path('ci-reports')
    path = folder / 'jmeter.csv'
    if not path.exists():
        print('JMeter result file missing')
        return 1
    with path.open(encoding='utf-8-sig', newline='') as source:
        rows = list(csv.DictReader(source))
    failed = sum(row.get('success') != 'true' for row in rows)
    result = {'kind': 'bounded smoke, not a capacity benchmark', 'expected_samples': 7,
        'samples': len(rows), 'failed': failed,
        'mean_ms': round(sum(float(row['elapsed']) for row in rows) / len(rows), 2) if rows else None}
    (folder / 'jmeter-summary.json').write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result))
    return int(len(rows) != 7 or failed != 0)


if __name__ == '__main__':
    raise SystemExit(main())
