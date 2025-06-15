from collections import Counter

def compare_txt_files(file1, file2):
    with open(file1, 'r', encoding='utf-8') as f1:
        lines1 = [line.strip() for line in f1.readlines() if line.strip()]
    with open(file2, 'r', encoding='utf-8') as f2:
        lines2 = [line.strip() for line in f2.readlines() if line.strip()]

    # 使用 Counter 统计每行出现的次数
    counter1 = Counter(lines1)
    counter2 = Counter(lines2)

    # 计算仅在 file1 中存在的行（考虑次数差异）
    only_in_file1 = []
    for line in counter1:
        if counter1[line] > counter2.get(line, 0):
            only_in_file1.extend([line] * (counter1[line] - counter2.get(line, 0)))

    # 计算仅在 file2 中存在的行（考虑次数差异）
    only_in_file2 = []
    for line in counter2:
        if counter2[line] > counter1.get(line, 0):
            only_in_file2.extend([line] * (counter2[line] - counter1.get(line, 0)))

    return only_in_file1, only_in_file2

# 示例使用
file1 = '1.txt'
file2 = '2.txt'

only1, only2 = compare_txt_files(file1, file2)

print("✅ 仅在 1.txt 中存在的行：")
for line in only1:
    print(line)

print("\n✅ 仅在 2.txt 中存在的行：")
for line in only2:
    print(line)
