import re

path = r'd:\4thYEAR\EAD\EAD_Assignment\EAD_Project\mobile\SmartSolarMobile\app\src\main\res\layout\activity_operator_dashboard.xml'

with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

if 'buttonSignOut' in content:
    print('Sign Out button already present — nothing to do.')
else:
    sign_out_xml = (
        '\n'
        '            <!-- Sign out button -->\n'
        '            <com.google.android.material.button.MaterialButton\n'
        '                android:id="@+id/buttonSignOut"\n'
        '                style="@style/Widget.Material3.Button.OutlinedButton"\n'
        '                android:layout_width="match_parent"\n'
        '                android:layout_height="52dp"\n'
        '                android:layout_marginBottom="24dp"\n'
        '                android:text="Sign Out"\n'
        '                android:textAllCaps="false"\n'
        '                android:textColor="#EF5350"\n'
        '                app:cornerRadius="12dp"\n'
        '                app:strokeColor="#EF5350"/>\n'
    )

    # Insert just before the closing body LinearLayout
    # The file ends with:  </LinearLayout>\n    </LinearLayout>\n</ScrollView>
    # (possibly CRLF or LF)
    for ending in [
        '        </LinearLayout>\r\n    </LinearLayout>\r\n</ScrollView>',
        '        </LinearLayout>\n    </LinearLayout>\n</ScrollView>',
    ]:
        if ending in content:
            content = content.replace(ending, sign_out_xml + ending, 1)
            with open(path, 'w', encoding='utf-8') as f:
                f.write(content)
            print('DONE — Sign Out button inserted.')
            break
    else:
        # Fallback: show the tail so we can debug
        print('TARGET NOT FOUND. Last 200 chars:')
        print(repr(content[-200:]))
