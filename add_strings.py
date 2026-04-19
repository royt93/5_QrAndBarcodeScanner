import os
import xml.etree.ElementTree as ET

res_dir = "/Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260221_QrAndBarcodeScanner/app/src/main/res"

new_strings = [
    ('mode_batch_scan', 'Batch Scan'),
    ('action_export_csv', 'Export CSV'),
    ('helper_batch_scan_title', 'Batch Scan Mode'),
    ('helper_batch_scan_message', 'Scan multiple items back-to-back without leaving the camera. Press "Export CSV" when you are done.'),
    ('security_alert_title', 'Security Warning'),
    ('security_alert_message', 'This QR code contains an unverified URL that may lead to a harmful website. We recommend avoiding this link.'),
    ('action_go_back_safe', 'Go Back (Safe)'),
    ('action_proceed_anyway', 'Proceed anyway')
]

for item in os.listdir(res_dir):
    if item.startswith("values"):
        strings_file = os.path.join(res_dir, item, "strings.xml")
        if os.path.exists(strings_file):
            with open(strings_file, 'r', encoding='utf-8') as f:
                content = f.read()

            inserted = False
            for name, val in new_strings:
                if f'name="{name}"' not in content:
                    # insert before </resources>
                    content = content.replace('</resources>', f'    <string name="{name}">{val}</string>\n</resources>')
                    inserted = True
            
            if inserted:
                with open(strings_file, 'w', encoding='utf-8') as f:
                    f.write(content)
print("Updated all strings.xml")
