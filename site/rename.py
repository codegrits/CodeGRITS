"""
Rename all the `ClassName.html` to `classname.html` in the /docs folder.
This is to make sure that the links in the JavaDoc is useful after the
processing by Retype.
"""
import os


def get_candidate_list(path):
    candidate_list = []
    for root, dirs, files in os.walk(path):
        for file in files:
            if file.endswith('.java'):
                candidate_list.append(file.replace('.java', '.html'))
    return candidate_list


def rename_text(file_path, candidate_list):
    with open(file_path, 'r') as f:
        content = f.read()
    for candidate in candidate_list:
        content = content.replace(candidate, candidate.lower())
    with open(file_path, 'w') as f:
        f.write(content)


if __name__ == '__main__':
    path_java = '../src/main/java'
    path_docs = './docs'
    candidates = get_candidate_list(path_java)
    for root, dirs, files in os.walk(path_docs):
        for file in files:
            if file.endswith('.html'):
                rename_text(os.path.join(root, file), candidates)
                if file in candidates:
                    os.rename(os.path.join(root, file), os.path.join(root, file.lower()))
