import csv
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def read_csv(path):
    with open(path) as f:
        return list(csv.DictReader(f))


os.makedirs("results/plots", exist_ok=True)

dp = read_csv("results/dp.csv")
n = [int(r["n"]) for r in dp]
plt.figure(figsize=(7, 4.5))
plt.plot(n, [float(r["lcs_ms"]) for r in dp], "o-", label="LCS")
plt.plot(n, [float(r["edit_ms"]) for r in dp], "s-", label="Edit Distance")
plt.xlabel("string length n (both strings)")
plt.ylabel("average time (ms)")
plt.title("LCS and Edit Distance: time vs input size")
plt.grid(alpha=0.3)
plt.legend()
plt.tight_layout()
plt.savefig("results/plots/dp_time.png", dpi=130)
plt.close()

matching = read_csv("results/matching.csv")
fig, axes = plt.subplots(1, 2, figsize=(11, 4.5), sharey=True)
for ax, case in zip(axes, ["present", "absent"]):
    rows = [r for r in matching if r["case"] == case]
    x = [int(r["n"]) for r in rows]
    ax.plot(x, [float(r["naive_ms"]) for r in rows], "o-", label="Naive")
    ax.plot(x, [float(r["kmp_ms"]) for r in rows], "s-", label="KMP")
    ax.plot(x, [float(r["rk_ms"]) for r in rows], "^-", label="Rabin-Karp")
    ax.set_title("random text, pattern " + case + " (m = 12)")
    ax.set_xlabel("text length n")
    ax.grid(alpha=0.3)
    ax.legend()
axes[0].set_ylabel("average time (ms)")
plt.tight_layout()
plt.savefig("results/plots/matching_time.png", dpi=130)
plt.close()

fig, axes = plt.subplots(1, 2, figsize=(11, 4.5))
for ax, case, title in zip(axes, ["worst_absent", "worst_present"],
                           ["text a...a, pattern a^99 b", "text a...a, pattern a^100"]):
    rows = [r for r in matching if r["case"] == case]
    x = [int(r["n"]) for r in rows]
    ax.plot(x, [float(r["naive_ms"]) for r in rows], "o-", label="Naive")
    ax.plot(x, [float(r["kmp_ms"]) for r in rows], "s-", label="KMP")
    ax.plot(x, [float(r["rk_ms"]) for r in rows], "^-", label="Rabin-Karp")
    ax.set_title(title)
    ax.set_xlabel("text length n")
    ax.set_ylabel("average time (ms)")
    ax.grid(alpha=0.3)
    ax.legend()
plt.tight_layout()
plt.savefig("results/plots/matching_worst_case.png", dpi=130)
plt.close()

kasai = read_csv("results/kasai.csv")
x = [int(r["n"]) for r in kasai]
fig, ax1 = plt.subplots(figsize=(7, 4.5))
ax1.plot(x, [float(r["kasai_ms"]) for r in kasai], "o-", color="tab:blue", label="time (ms)")
ax1.set_xlabel("string length n")
ax1.set_ylabel("average time (ms)", color="tab:blue")
ax2 = ax1.twinx()
ax2.plot(x, [int(r["comparisons"]) for r in kasai], "s--", color="tab:orange", label="char comparisons")
ax2.set_ylabel("character comparisons", color="tab:orange")
plt.title("Kasai LCP: time and comparisons vs n")
ax1.grid(alpha=0.3)
fig.tight_layout()
plt.savefig("results/plots/kasai.png", dpi=130)
plt.close()

print("Saved plots to results/plots/")
